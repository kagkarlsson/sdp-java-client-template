import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.*;
import no.difi.sdp.client.domain.digital_post.DigitalPost;
import no.difi.sdp.client.domain.digital_post.EpostVarsel;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.client.domain.digital_post.SmsVarsel;
import no.difi.sdp.client.domain.fysisk_post.*;
import no.difi.sdp.client.domain.kvittering.Feil;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.VarslingFeiletKvittering;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static no.difi.sdp.client.domain.fysisk_post.Landkoder.Predefinert.SVERIGE;
import static org.apache.commons.lang3.Validate.notNull;

public class OffentligSender {

	private static final Config CONFIG = Config.PREPROD;

	private static final Logger LOG = LoggerFactory.getLogger(OffentligSender.class);
	private final KlientKonfigurasjon klientKonfigurasjon = CONFIG.getKlientKonfigurasjon();

	private final Behandlingsansvarlig sdpAvsender =
			Behandlingsansvarlig
					.builder("984661185")
					.avsenderIdentifikator("digipost")
					.build();

	private final TekniskAvsender tekniskAvsender = TekniskAvsender.builder("984661185", CONFIG.getNoekkelpar()).build();
	private final SikkerDigitalPostKlient postklient = new SikkerDigitalPostKlient(tekniskAvsender, klientKonfigurasjon);

	private static final Prioritet PRIORITET = Prioritet.NORMAL;
	private final String mpc_o_lados = "minmpc";

	@Test
	public void sende_digital() throws IOException {

		String emne = "Mitt brevemne";

		String fnr = "01048200229";
		Mottaker mottaker = Mottaker.builder(fnr,"preben.montebello#8AB5", CONFIG.getKrypteringssert(), "984661185").build();

		DigitalPost digitalPost = DigitalPost.builder(mottaker, emne)
				.sikkerhetsnivaa(Sikkerhetsnivaa.NIVAA_3)
				.build();

		Dokument hovedDokument = Dokument.builder(emne, "printvennlig", OffentligSender.class.getClass().getResourceAsStream("/printvennlig.pdf"))
				.mimeType("application/pdf")
				.build();
		Dokumentpakke dokumentPakke = Dokumentpakke.builder(hovedDokument).build();

		String konversasjonsId = UUID.randomUUID().toString();
		LOG.info("Sender melding med emne " + digitalPost.getIkkeSensitivTittel() + ", konversasjons-ID: " + konversasjonsId);

		Forsendelse forsendelse =
				Forsendelse.digital(sdpAvsender, digitalPost, dokumentPakke)
						.konversasjonsId(konversasjonsId)
						.prioritet(PRIORITET)
						.spraakkode("NO")
						.mpcId(mpc_o_lados)
						.build();

		postklient.send(forsendelse);

		LOG.info("Sendte melding med conversationID {}", konversasjonsId);
	}

	@Test
	public void henteKvitteringer() {
		for (ForretningsKvittering kvittering : new HentKvitteringer(postklient, mpc_o_lados, PRIORITET)) {
    		if (kvittering instanceof Feil) {
    			Feil feil = (Feil) kvittering;
    			LOG.warn("Feilkvittering {}: {}", feil.getFeiltype(), feil.getDetaljer());
    		} else if (kvittering instanceof VarslingFeiletKvittering) {
    			VarslingFeiletKvittering varslingFeilet = (VarslingFeiletKvittering) kvittering;
    			LOG.warn("Varsling feilet ({}): {}", varslingFeilet.getVarslingskanal(), varslingFeilet.getBeskrivelse());
    		} else {
    			LOG.info("Kvittering av typen " + kvittering.getClass().getSimpleName() + ", konv-id: " + kvittering.getKonversasjonsId());
    		}
		}
	}

}
