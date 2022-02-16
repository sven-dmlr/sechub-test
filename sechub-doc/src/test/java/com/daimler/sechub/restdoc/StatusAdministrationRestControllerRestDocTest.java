// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocFactory;
import com.daimler.sechub.domain.administration.mapping.FetchMappingService;
import com.daimler.sechub.domain.administration.mapping.MappingAdministrationRestController;
import com.daimler.sechub.domain.administration.mapping.UpdateMappingService;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdmiUpdatesMappingConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesMappingConfiguration;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
import com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter;
import com.epages.restdocs.apispec.ResourceSnippetParameters;

@RunWith(SpringRunner.class)
@WebMvcTest(MappingAdministrationRestController.class)
@ContextConfiguration(classes = { MappingAdministrationRestController.class, MappingAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class StatusAdministrationRestControllerRestDocTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    FetchMappingService fetchMappingService;

    @MockBean
    UpdateMappingService updateMappingService;

    private MappingData mappingDataTeam;

    @Before
    public void before() {
        mappingDataTeam = new MappingData();
        mappingDataTeam.getEntries().add(new MappingEntry("testproject_*", "8be4e3d4-6b53-4636-b65a-949a9ebdf6b9", "testproject-team"));
        mappingDataTeam.getEntries().add(new MappingEntry(".*", "3be4e3d2-2b55-2336-b65a-949b9ebdf6b9", "default-team"));
        /*
         * there could be more status examples in future - currently only scheduler
         * status info available
         */
        when(fetchMappingService.fetchMappingData(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId())).thenReturn(mappingDataTeam);
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminFetchesMappingConfiguration.class)
    public void restdoc_admin_fetches_mapping_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetMapping(MAPPING_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminFetchesMappingConfiguration.class;

        /* execute + test @formatter:off */

		this.mockMvc.perform(
				get(apiEndpoint, MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId()).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*
				*/
		andDo(print()).
		andExpect(status().isOk()).
		andDo(document(RestDocFactory.createPath(useCase),
		        resource(
		                ResourceSnippetParameters.builder().
		                    summary(RestDocFactory.createSummary(useCase)).
		                    description(RestDocFactory.createDescription(useCase)).
		                    tag(RestDocFactory.extractTag(apiEndpoint)).
        		            responseSchema(OpenApiSchema.MAPPING_CONFIGURATION.getSchema()).
                            pathParameters(
                                    parameterWithName(MAPPING_ID.paramName()).description("The mapping Id")
                            ).
                            responseFields(
                                    fieldWithPath(MappingData.PROPERTY_ENTRIES+".[]."+MappingEntry.PROPERTY_PATTERN).description("Pattern"),
                                    fieldWithPath(MappingData.PROPERTY_ENTRIES+".[]."+MappingEntry.PROPERTY_REPLACEMENT).description("Replacement"),
                                    fieldWithPath(MappingData.PROPERTY_ENTRIES+".[]."+MappingEntry.PROPERTY_COMMENT).description("Comment")
                            ).
                            build()
        		        )
		        ));

	      /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdmiUpdatesMappingConfiguration.class)
    public void restdoc_admin_updates_mapping_configuration() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUpdateMapping(RestDocPathParameter.MAPPING_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdmiUpdatesMappingConfiguration.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                put(apiEndpoint, MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId()).
                contentType(MediaType.APPLICATION_JSON_VALUE).
                content(mappingDataTeam.toJSON())
                )./*
                */
        andDo(print()).
        andExpect(status().isOk()).
        andDo(document(RestDocFactory.createPath(useCase),
                resource(
                        ResourceSnippetParameters.builder().
                            summary(RestDocFactory.createSummary(useCase)).
                            description(RestDocFactory.createDescription(useCase)).
                            tag(RestDocFactory.extractTag(apiEndpoint)).
                            pathParameters(
                                    parameterWithName(MAPPING_ID.paramName()).description("The mappingID, identifiying which mapping shall be updated")
                                ).
                            requestFields(
                                    fieldWithPath(MappingData.PROPERTY_ENTRIES+".[]."+MappingEntry.PROPERTY_PATTERN).description("Pattern"),
                                    fieldWithPath(MappingData.PROPERTY_ENTRIES+".[]."+MappingEntry.PROPERTY_REPLACEMENT).description("Replacement"),
                                    fieldWithPath(MappingData.PROPERTY_ENTRIES+".[]."+MappingEntry.PROPERTY_COMMENT).description("Comment")
                            ).
                            requestSchema(OpenApiSchema.MAPPING_CONFIGURATION.getSchema()).
                            build()
                        )
                ));

        /* @formatter:on */
    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }
}
