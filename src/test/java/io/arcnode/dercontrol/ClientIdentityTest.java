package io.arcnode.dercontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Unit — LFDI/SFDI derivation from the nginx-forwarded {@code X-SSL-Client-Cert} header (IEEE
 * 2030.5 §6.3.4). AAA.
 */
class ClientIdentityTest {

  @Test
  void derivesLfdiAndSfdiFromUrlEncodedPemHeader() {
    // Arrange / Act
    ClientIdentity identity = ClientIdentity.fromHeaderValue(TestCerts.HEADER_VALUE);

    // Assert
    assertThat(identity.lfdi()).isEqualTo(TestCerts.LFDI);
    assertThat(identity.sfdi()).isEqualTo(TestCerts.SFDI);
  }

  @Test
  void rejectsMalformedCertificate() {
    // Arrange
    String header = URLEncoder.encode("not a certificate", StandardCharsets.UTF_8);

    // Act / Assert
    assertThatThrownBy(() -> ClientIdentity.fromHeaderValue(header))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void checkDigitMakesDigitSumDivisibleByTen() {
    // Arrange: hand-verified example — 5 bytes 0x00_00_00_00_10 -> 40-bit 16 -> >>4 -> 36-bit 1
    byte[] lfdiPrefix = {0x00, 0x00, 0x00, 0x00, 0x10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // Act
    long sfdi = ClientIdentity.sfdiFromLfdiBytes(lfdiPrefix);

    // Assert: 36-bit value 1, digit sum 1, check digit 9 -> 19
    assertThat(sfdi).isEqualTo(19L);
  }
}
