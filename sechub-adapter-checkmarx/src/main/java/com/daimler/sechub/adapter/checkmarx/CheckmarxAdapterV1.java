// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.AdapterRuntimeContext;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxFullScanNecessaryException;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxOAuthSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxProjectSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxScanReportSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxScanSupport;
import com.daimler.sechub.adapter.checkmarx.support.CheckmarxUploadSupport;

/**
 * This component is able to handle results from
 * <ol>
 * <li>Checkmarx V8.8.0 HF1</li>
 * </ol>
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class CheckmarxAdapterV1 extends AbstractAdapter<CheckmarxAdapterContext, CheckmarxAdapterConfig> implements CheckmarxAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxAdapterV1.class);

    @Override
    public String execute(CheckmarxAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        try {
            assertNotInterrupted();

            CheckmarxContext context = new CheckmarxContext(config, this, runtimeContext);
            context.setFullScan(context.isNewProject() || config.isAlwaysFullScanEnabled());

            CheckmarxOAuthSupport oauthSupport = new CheckmarxOAuthSupport();
            oauthSupport.loginAndGetOAuthToken(context);

            assertNotInterrupted();
            /* ensure project and get project context */
            CheckmarxProjectSupport projectSupport = new CheckmarxProjectSupport();
            projectSupport.ensureProjectExists(context);

            assertNotInterrupted();
            handleUploadSourceCodeAndStartScan(oauthSupport, context);

            assertNotInterrupted();
            CheckmarxScanReportSupport scanReportSupport = new CheckmarxScanReportSupport();
            scanReportSupport.startFetchReport(oauthSupport, context);

            return context.getResult();
        } catch (Exception e) {
            throw asAdapterException("Was not able to perform scan!", e, config);
        }

    }

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    private void handleUploadSourceCodeAndStartScan(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        try {
            uploadSourceCodeAndStartScan(oauthSupport, context);
        } catch (CheckmarxFullScanNecessaryException e) {
            LOG.info("Full scan necessary bcause of checkmarx message: {}", e.getCheckmarxMessage());
            context.setFullScan(true);
            uploadSourceCodeAndStartScan(oauthSupport, context);

        }
    }

    private void uploadSourceCodeAndStartScan(CheckmarxOAuthSupport oauthSupport, CheckmarxContext context) throws AdapterException {
        AdapterMetaData metaData = context.getRuntimeContext().getMetaData();
        if (!metaData.hasValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            /* upload source code */
            oauthSupport.refreshBearerTokenWhenNecessary(context);

            CheckmarxUploadSupport uploadSupport = new CheckmarxUploadSupport();
            uploadSupport.uploadZippedSourceCode(context);

            /* after this - mark file upload done, so on a restart we don't need this */
            metaData.setValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true);
            context.getRuntimeContext().getCallback().persist(metaData);
        } else {
            LOG.info("Reuse existing upload for:{}", context.getTraceID());
        }
        /* start scan */
        CheckmarxScanSupport scanSupport = new CheckmarxScanSupport();
        scanSupport.startNewScan(oauthSupport, context);
    }

    @Override
    protected String getAPIPrefix() {
        return "cxrestapi";
    }
}
