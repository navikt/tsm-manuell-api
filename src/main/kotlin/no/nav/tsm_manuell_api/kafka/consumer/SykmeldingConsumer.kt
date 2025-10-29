package no.nav.tsm_manuell_api.kafka.consumer

import no.nav.tsm_manuell_api.utils.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SykmeldingConsumer(
    @param:Value($$"${nais.cluster}") private val clusterName: String,
) {
    private val logger = logger()

    // @Transactional
    @KafkaListener(
        topics = [$$"${kafka.topics.sykmeldinger}"],
        groupId = "tsm-manuell-api-consumer",
        containerFactory = "kafkaListenerContainerFactory",
        batch = "false",
    )
    fun consume(record: ConsumerRecord<String, ByteArray?>) {
        val sykmeldingId = record.key()
        // logger.info("$clusterName sykmeldingId=$sykmeldingId")
    }
}
