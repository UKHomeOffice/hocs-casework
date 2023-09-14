package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:correspondent/beforeTest.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:correspondent/afterTest.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = ISOLATED))
@ActiveProfiles({"local"})
public class CorrespondentResourceIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void whenListOfCorrespondentsIsRequested_activeCorrespondentsForActiveCasesAreSent() throws Exception {
        mvc.perform(get("/correspondents"))
           .andExpect(request().asyncStarted())
           .andDo(MvcResult::getAsyncResult)
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"cb4af956-4946-4a2d-9bd6-aee429df7396\" && @.fullname == \"ActiveOne CaseOne\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"64ffd23c-2f3c-44b4-84ee-5b2ce0eafc40\" && @.fullname == \"ActiveOne CaseTwo\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"9ded5e1c-f7f7-4930-bc19-f56518746721\" && @.fullname == \"ActiveTwo CaseTwo\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"c09d94ab-8156-49d6-b7f1-758637c7c048\" && @.fullname == \"ActiveOne CaseThree\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"bc33ca22-77c0-451a-a896-950d01005c97\")]").doesNotExist())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"457e967c-a194-4f15-a0c8-9ff4ce0b436a\")]").doesNotExist())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"0885df7e-aeec-4fc8-a726-767749359736\")]").doesNotExist());
    }

    @Test
    public void whenListOfCorrespondentsIncludingDeletedIsRequested_allCorrespondentsForActiveCasesAreSent() throws Exception {
        mvc.perform(get("/correspondents?includeDeleted=true"))
           .andExpect(request().asyncStarted())
           .andDo(MvcResult::getAsyncResult)
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"cb4af956-4946-4a2d-9bd6-aee429df7396\" && @.fullname == \"ActiveOne CaseOne\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"64ffd23c-2f3c-44b4-84ee-5b2ce0eafc40\" && @.fullname == \"ActiveOne CaseTwo\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"9ded5e1c-f7f7-4930-bc19-f56518746721\" && @.fullname == \"ActiveTwo CaseTwo\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"c09d94ab-8156-49d6-b7f1-758637c7c048\" && @.fullname == \"ActiveOne CaseThree\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"bc33ca22-77c0-451a-a896-950d01005c97\" && @.fullname == \"DeletedOne CaseThree\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"457e967c-a194-4f15-a0c8-9ff4ce0b436a\" && @.fullname == \"DeletedOne CaseFour\")]").exists())
           .andExpect(jsonPath("$.correspondents[?(@.uuid == \"0885df7e-aeec-4fc8-a726-767749359736\")]").doesNotExist());
    }
}
