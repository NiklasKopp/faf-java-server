package com.faforever.server.geoip;

import com.faforever.server.config.ServerProperties;
import com.faforever.server.config.ServerProperties.GeoIp;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GeoIpServiceTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private GeoIpService instance;

  @Before
  public void setUp() throws Exception {
    Path databaseFile = temporaryFolder.getRoot().toPath().resolve("cache.mmdb");
    assertThat(Files.exists(databaseFile), is(false));

    ServerProperties properties = new ServerProperties();
    GeoIp geoIp = properties.getGeoIp();
    geoIp.setDatabaseUrl(getClass().getResource("/geoip/GeoLite2-City.mmdb.gz").toURI().toString());
    geoIp.setDatabaseFile(databaseFile);

    instance = new GeoIpService(properties);
    instance.postConstruct();

    assertThat(Files.exists(databaseFile), is(true));
  }

  @Test
  public void lookupCountry() throws Exception {
    // Let's see how long the NASA servers stay in the US under the Trump administration
    Optional<String> country = instance.lookupCountryCode(InetAddress.getByName("192.203.230.10"));
    assertThat(country.get(), is("US"));
  }

  @Test
  public void lookupCountryLoopbackReturnsEmpty() throws Exception {
    Optional<String> result = instance.lookupCountryCode(InetAddress.getLoopbackAddress());
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void lookupTimezone() throws Exception {
    // Timezone data is not available for all ip addresses, alternatively use MaxMind server (108.168.255.243)
    Optional<TimeZone> timeZone = instance.lookupTimezone(InetAddress.getByName("85.214.28.212"));
    assertThat(timeZone.get().getID(), is("Europe/Berlin"));
  }

  @Test
  public void lookupTimezoneUnknownReturnsEmpty() throws Exception {
    Optional<TimeZone> result = instance.lookupTimezone(InetAddress.getByName("192.203.230.10"));
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void lookupTimezoneLoopbackReturnsEmpty() throws Exception {
    Optional<TimeZone> result = instance.lookupTimezone(InetAddress.getLoopbackAddress());
    assertThat(result.isPresent(), is(false));
  }
}
