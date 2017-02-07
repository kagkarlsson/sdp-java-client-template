import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.KvitteringForespoersel;
import no.motif.iter.SimpleIterator;
import no.motif.single.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static no.motif.Singular.optional;


public class HentKvitteringer implements Iterable<ForretningsKvittering> {

	private static final Logger LOG = LoggerFactory.getLogger(HentKvitteringer.class);

	private final SikkerDigitalPostKlient postklient;
	private final Prioritet prioritet;
	private final String mpc;

	public HentKvitteringer(SikkerDigitalPostKlient klient, String mpc) {
		this(klient, mpc, Prioritet.NORMAL);
	}


	public HentKvitteringer(SikkerDigitalPostKlient klient, String mpc, Prioritet prioritet) {
		this.postklient = klient;
		this.prioritet = prioritet;
		this.mpc = mpc;
	}

	@Override
    public Iterator<ForretningsKvittering> iterator() {
		return new SimpleIterator<ForretningsKvittering>() {
			@Override
            protected Optional<ForretningsKvittering> nextIfAvailable() {
				ForretningsKvittering kvittering = postklient.hentKvittering(KvitteringForespoersel.builder(prioritet).mpcId(mpc).build());
				if (kvittering != null) {
					LOG.info("Kvittering type '{}', conversationId {}, ref-to: {}", kvittering.getClass().getSimpleName(), kvittering.getKonversasjonsId(), kvittering.getRefToMessageId());
					postklient.bekreft(kvittering);
				} else {
					LOG.info("Fikk ingen flere kvitteringer");
				}
				return optional(kvittering);
            }};
    }
}