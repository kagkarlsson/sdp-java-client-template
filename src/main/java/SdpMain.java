import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.*;
import no.difi.sdp.client.domain.digital_post.DigitalPost;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SdpMain {
	private static final Logger LOG = LoggerFactory.getLogger(SdpMain.class);

	private static final Config CONFIG = Config.PREPROD;
	private static final KlientKonfigurasjon KLIENT_KONFIGURASJON = CONFIG.getKlientKonfigurasjon();

	private static final Behandlingsansvarlig sdpAvsender =
			Behandlingsansvarlig
					.builder("984661185")
					.avsenderIdentifikator("digipost")
					.build();

	private static final TekniskAvsender TEKNISK_AVSENDER = TekniskAvsender.builder("984661185", CONFIG.getNoekkelpar()).build();
	private static final SikkerDigitalPostKlient POST_KLIENT = new SikkerDigitalPostKlient(TEKNISK_AVSENDER, KLIENT_KONFIGURASJON);

	private static final Prioritet PRIORITET = Prioritet.NORMAL;
	private static final String MPC = "minmpc";


	public static void main(String[] args) throws IOException {
		String emne = "Mitt brevemne";

		String fnr = "01048200229";
		Mottaker mottaker = Mottaker.builder(fnr,"preben.montebello#8AB5", CONFIG.getKrypteringssert(), "984661185").build();

		DigitalPost digitalPost = DigitalPost.builder(mottaker, emne)
				.sikkerhetsnivaa(Sikkerhetsnivaa.NIVAA_3)
				.build();

		Dokument hovedDokument = Dokument.builder(emne, "printvennlig", FileUtils.openInputStream(new File("src/main/resources/printvennlig.pdf")))
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
						.mpcId(MPC)
						.build();

		POST_KLIENT.send(forsendelse);

		LOG.info("Sendte melding med conversationID {}", konversasjonsId);
	}


//	public void henteKvitteringer() {
//		for (ForretningsKvittering kvittering : new HentKvitteringer(postklient, MPC, PRIORITET)) {
//    		if (kvittering instanceof Feil) {
//    			Feil feil = (Feil) kvittering;
//    			LOG.warn("Feilkvittering {}: {}", feil.getFeiltype(), feil.getDetaljer());
//    		} else if (kvittering instanceof VarslingFeiletKvittering) {
//    			VarslingFeiletKvittering varslingFeilet = (VarslingFeiletKvittering) kvittering;
//    			LOG.warn("Varsling feilet ({}): {}", varslingFeilet.getVarslingskanal(), varslingFeilet.getBeskrivelse());
//    		} else {
//    			LOG.info("Kvittering av typen " + kvittering.getClass().getSimpleName() + ", konv-id: " + kvittering.getKonversasjonsId());
//    		}
//		}
//	}

}
