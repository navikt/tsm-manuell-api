package no.nav.tsm_manuell_api.metrics

import io.prometheus.client.Counter

const val METRICS_NS = "tsm_manuell_api"

val MESSAGE_STORED_IN_DB_COUNTER: Counter =
    Counter.build()
        .namespace(METRICS_NS)
        .name("message_stored_in_db_count")
        .help("Counts the number of messages stored in db")
        .register()

val OPPRETT_OPPGAVE_COUNTER: Counter =
    Counter.Builder()
        .namespace(METRICS_NS)
        .name("opprett_oppgave_counter")
        .help("Registers a counter for each oppgave that is created")
        .register()

val INCOMING_MESSAGE_COUNTER: Counter =
    Counter.build()
        .namespace(METRICS_NS)
        .name("incoming_message_count")
        .help("Counts the number of incoming messages")
        .register()

val SYKMELDING_COUNT_STATUS: Counter =
    Counter.build()
        .namespace(METRICS_NS)
        .name("sykmelding_count_status")
        .help("Counts the number processed sykmeldinger and count for each status")
        .labelNames("status")
        .register()
