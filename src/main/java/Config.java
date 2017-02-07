import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.domain.Noekkelpar;
import no.difi.sdp.client.domain.Sertifikat;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public enum Config {

	PREPROD("https://qaoffentlig.meldingsformidler.digipost.no/api/ebms/9908:984661185", Config::noekkelparMF, Constants.BUYPASSTEST_DIGI_KRYPT_TEST);

	public KlientKonfigurasjon getKlientKonfigurasjon() {
		KlientKonfigurasjon.Builder builder = KlientKonfigurasjon.builder()
				.meldingsformidlerRoot(url)
				.connectionTimeout(10, TimeUnit.SECONDS);
		return builder.build();
	}

	public Noekkelpar getNoekkelpar() {
		return signeringSert.get();
	}

	public Sertifikat getKrypteringssert() {
		return krypteringsSert;
	}

	public static class Constants {
		public static final Sertifikat BUYPASSTEST_DIGI_KRYPT_TEST = Sertifikat.fraBase64X509String("MIIE7jCCA9agAwIBAgIKGBZrmEgzTHzeJjANBgkqhkiG9w0BAQsFADBRMQswCQYDVQQGEwJOTzEdMBsGA1UECgwUQnV5cGFzcyBBUy05ODMxNjMzMjcxIzAhBgNVBAMMGkJ1eXBhc3MgQ2xhc3MgMyBUZXN0NCBDQSAzMB4XDTE0MDQyNDEyMzA1MVoXDTE3MDQyNDIxNTkwMFowVTELMAkGA1UEBhMCTk8xGDAWBgNVBAoMD1BPU1RFTiBOT1JHRSBBUzEYMBYGA1UEAwwPUE9TVEVOIE5PUkdFIEFTMRIwEAYDVQQFEwk5ODQ2NjExODUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCLCxU4oBhtGmJxXZWbdWdzO2uA3eRNW/kPdddL1HYl1iXLV/g+H2Q0ELadWLggkS+1kOd8/jKxEN++biMmmDqqCWbzNdmEd1j4lctSlH6M7tt0ywmXIYdZMz5kxcLAMNXsaqnPdikI9uPJZQEL3Kc8hXhXISvpzP7gYOvKHg41uCxu1xCZQOM6pTlNbxemBYqvES4fRh2xvB9aMjwkB4Nz8jrIsyoPI89i05OmGMkI5BPZt8NTa40Yf3yU+SQECW0GWalB5cxaTMeB01tqslUzBJPV3cQx+AhtQG4hkOhQnAMDJramSPVtwbEnqOjQ+lyNmg5GQ4FJO02ApKJTZDTHAgMBAAGjggHCMIIBvjAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFD+u9XgLkqNwIDVfWvr3JKBSAfBBMB0GA1UdDgQWBBQ1gsJfVC7KYGiWVLP7ZwzppyVYTTAOBgNVHQ8BAf8EBAMCBLAwFgYDVR0gBA8wDTALBglghEIBGgEAAwIwgbsGA1UdHwSBszCBsDA3oDWgM4YxaHR0cDovL2NybC50ZXN0NC5idXlwYXNzLm5vL2NybC9CUENsYXNzM1Q0Q0EzLmNybDB1oHOgcYZvbGRhcDovL2xkYXAudGVzdDQuYnV5cGFzcy5uby9kYz1CdXlwYXNzLGRjPU5PLENOPUJ1eXBhc3MlMjBDbGFzcyUyMDMlMjBUZXN0NCUyMENBJTIwMz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0MIGKBggrBgEFBQcBAQR+MHwwOwYIKwYBBQUHMAGGL2h0dHA6Ly9vY3NwLnRlc3Q0LmJ1eXBhc3Mubm8vb2NzcC9CUENsYXNzM1Q0Q0EzMD0GCCsGAQUFBzAChjFodHRwOi8vY3J0LnRlc3Q0LmJ1eXBhc3Mubm8vY3J0L0JQQ2xhc3MzVDRDQTMuY2VyMA0GCSqGSIb3DQEBCwUAA4IBAQCe67UOZ/VSwcH2ov1cOSaWslL7JNfqhyNZWGpfgX1c0Gh+KkO3eVkMSozpgX6M4eeWBWJGELMiVN1LhNaGxBU9TBMdeQ3SqK219W6DXRJ2ycBtaVwQ26V5tWKRN4UlRovYYiY+nMLx9VrLOD4uoP6fm9GE5Fj0vSMMPvOEXi0NsN+8MUm3HWoBeUCLyFpe7/EPsS/Wud5bb0as/E2zIztRodxfNsoiXNvWaP2ZiPWFunIjK1H/8EcktEW1paiPd8AZek/QQoG0MKPfPIJuqH+WJU3a8J8epMDyVfaek+4+l9XOeKwVXNSOP/JSwgpOJNzTdaDOM+uVuk75n2191Fd7");
	}

	private final String url;
	private final Supplier<Noekkelpar> signeringSert;
	private final Sertifikat krypteringsSert;

	Config(String url, Supplier<Noekkelpar> signeringSert, Sertifikat krypteringsSert) {
		this.url = url;
		this.signeringSert = signeringSert;
		this.krypteringsSert = krypteringsSert;
	}

	public static Noekkelpar noekkelparMF() {
		//TODO: password in ENV for prod-version..
		return Noekkelpar.fraKeyStore(loadOffentligKeystore(), "meldingsformidler", "abcd1234");
	}

	private static KeyStore loadOffentligKeystore() {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance("JCEKS");
			//TODO: password in ENV for prod-version..
			keyStore.load(new FileInputStream(new File("klient-trust-og-sender-serts.jce")), "abcd1234".toCharArray());
		} catch (Exception e) {
			throw new RuntimeException("Kunne ikke laste keystore", e);
		}
		return keyStore;
	}

}
