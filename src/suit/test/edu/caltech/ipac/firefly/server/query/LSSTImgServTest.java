/*
 * License information at https://github.com/Caltech-IPAC/firefly/blob/master/License.txt
 */
package edu.caltech.ipac.firefly.server.query;

import edu.caltech.ipac.firefly.ConfigTest;
import edu.caltech.ipac.firefly.data.FileInfo;
import edu.caltech.ipac.firefly.server.query.lsst.LSSTImageSearch;
import edu.caltech.ipac.firefly.server.query.lsst.LSSTQuery;
import edu.caltech.ipac.util.download.FailedRequestException;
import edu.caltech.ipac.util.download.URLDownload;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Test getting images from DAX ImageServ, when it's available 
 */
public class LSSTImgServTest extends ConfigTest {

    @BeforeClass
   	public static void setUp() {
   		setupServerContext(null);
   	}

    @Test
   	public void testDaxImages() {
   		try {
            if (imgservAvailable()) {
                // calexp exposure
                String ccdUrl = LSSTImageSearch.createURLForScienceCCD("5646", "4", "694", "g");
                // deep coadd image
                String deepCoaddUrl = LSSTImageSearch.createURLForDeepCoadd("0", "225,1", "r");
                // calexp cutout
                String ccdCutoutUrl = LSSTImageSearch.getImgBaseUrl()+"?ds=calexp&sid=5646240694&ra=37.6292&dec=0.104625&width=300.0&height=450.0&unit=arcsec";
                // deep coadd cutout
                String deepCoaddCutoutUrl = LSSTImageSearch.getImgBaseUrl()+"?ds=deepcoadd&ra=19.36995&dec=-0.3147&filter=r&width=300&height=400&unit=arcsec";

                String[] urls = {ccdUrl, deepCoaddUrl, ccdCutoutUrl, deepCoaddCutoutUrl};
                boolean passed;
                for (String url : urls) {
                    passed = getImage(url);
                    Assert.assertTrue("FAILED: "+url, passed);
                }
            }
   		} catch (Exception e) {
   			Assert.fail("testDaxImages failed with exception: " + e.getMessage());
   		}
   	}

    private boolean getImage(String url) {
        try {
            File file = File.createTempFile("lssttest", "json");
            file.deleteOnExit();

            long cTime = System.currentTimeMillis();
            FileInfo fileData = URLDownload.getDataToFile(new URL(url), file);
            LOG.info("Image retrieval took " + (System.currentTimeMillis() - cTime) + "ms");
            LOG.info(url);

            if (fileData.getResponseCode() >= 400) {
                String err = fileData.getResponseCode()+" "+fileData.getResponseCodeMsg();
                LOG.error(err);
                return false;
            } else {
                return true;
            }
        } catch (FailedRequestException | IOException e) {
            LOG.error(e, url);
            return false;
        }
    }

   	private static boolean imgservAvailable() {
        try {
      			URL urlServer = new URL(LSSTQuery.getImgservURL());
      			HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
      			urlConn.setConnectTimeout(3000); // 3 seconds timeout
      			urlConn.connect();
      			return urlConn.getResponseCode() == 200;
      		} catch (IOException e) {
                LOG.info("imgserv is not available "+e.getMessage());
      			return false;
      		}
    }
}
