package io.eichler.spirekeystore

import SpiffeWorkloadAPIGrpc
import Workload
import io.grpc.*
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import io.netty.channel.kqueue.KQueueDomainSocketChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.unix.DomainSocketAddress
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


@Component
class SpiffeSvidRetrieval() {
    private val logger = LoggerFactory.getLogger(SpiffeSvidRetrieval::class.java)


    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())!!;

    @Bean
    fun provideKeyStore(): KeyStore {
        return keyStore
    }

    init {
        keyStore.load(null, "".toCharArray())
        Security.addProvider(BouncyCastleProvider());
    }

    // TODO differentiate between linux and mac native channels
    // TODO make socket configurable
    private val spiffeStub: SpiffeWorkloadAPIGrpc.SpiffeWorkloadAPIBlockingStub
        get() {
            val originalChannel = NettyChannelBuilder.forAddress(DomainSocketAddress("/tmp/agent.sock"))
                    .eventLoopGroup(KQueueEventLoopGroup())
                    .channelType(KQueueDomainSocketChannel::class.java)
                    .negotiationType(NegotiationType.PLAINTEXT)
                    .build();
            val channel = ClientInterceptors.intercept(originalChannel, HeaderClientInterceptor())


            val blockingStub = SpiffeWorkloadAPIGrpc.newBlockingStub(channel)

            @Throws(InterruptedException::class)
            fun shutdown() {
                originalChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
            }
            return blockingStub
        }

    @PostConstruct
    @Scheduled(fixedRate = 5 * 60 * 1000)
    fun retrieveAndConfigureCertificates() {
        val request = Workload.X509SVIDRequest.newBuilder().build()
        val response = spiffeStub.fetchX509SVID(request)
        if (!response.hasNext()) {
            logger.error("Received empty result from Spiffe server. No SSL configuration will be possible")
        }
        response.next().svidsList.forEach { svid -> processSvid(svid) }
    }

    private fun generateCertificates(input: ByteArray): Collection<Certificate> {
        return CertificateFactory.getInstance("X509").generateCertificates(ByteArrayInputStream(input))
    }

    private fun processSvid(svid: Workload.X509SVID) {

        val certificates: Collection<Certificate> = generateCertificates(svid.x509Svid.toByteArray())
        val caCertificates: Collection<Certificate> = generateCertificates(svid.bundle.toByteArray())
        val chain = certificates.plus(caCertificates).toTypedArray()

        val alias = svid.spiffeId
        logger.info("Successfully retrieved SVID: $alias")

        val kf = KeyFactory.getInstance("ECDSA", BouncyCastleProvider())
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(svid.x509SvidKey.toByteArray()));


        keyStore.setKeyEntry("default", privateKey, "".toCharArray(), chain)
        val certificate = keyStore.getCertificate("default")
        if (certificate == null || !certificate.equals(certificates.first())) {
            keyStore.setCertificateEntry("default", certificates.first())
        }
        caCertificates.forEach { c ->
            keyStore.setCertificateEntry("$alias-ca-until-${(c as X509Certificate).notAfter}", c)
        }
        logger.info("Added ${caCertificates.size} for $alias")
    }


    class HeaderClientInterceptor : ClientInterceptor {
        val headerKey = io.grpc.Metadata.Key.of("workload.spiffe.io", io.grpc.Metadata.ASCII_STRING_MARSHALLER)!!

        override fun <ReqT : Any?, RespT : Any?> interceptCall(method: MethodDescriptor<ReqT, RespT>?, callOptions: CallOptions?, next: Channel?): ClientCall<ReqT, RespT> {
            return object : SimpleForwardingClientCall<ReqT, RespT>(next?.newCall(method, callOptions)) {

                override fun start(responseListener: ClientCall.Listener<RespT>, headers: Metadata) {
                    headers.put(headerKey, "true")
                    super.start(object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {}, headers)
                }
            }
        }
    }


}
