package com.coeux.todo;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.coeux.todo.controllers.ActivityController;
import com.coeux.todo.entities.Activity;
import com.coeux.todo.entities.ActivityState;
import com.coeux.todo.entities.ActivityStatus;
import com.coeux.todo.entities.ActivityType;
import com.coeux.todo.entities.Label;
import com.coeux.todo.entities.MUser;
import com.coeux.todo.entities.Media;
import com.coeux.todo.filters.SecurityConfig;
import com.coeux.todo.jwt.JwtTokenAuthenticationProvider;
import com.coeux.todo.services.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@WebMvcTest(ActivityController.class)
@ContextConfiguration(classes = { SecurityConfig.class, TodoApplication.class })
@AutoConfigureRestDocs(outputDir = "target/snippets")
@ExtendWith(MockitoExtension.class)
public class AcitivityAPIDocsTests {

    private static final String TOKEN = "user_token";
    private static final MUser MUSER = new MUser(UUID.randomUUID());

    private Snippet pathParameters = pathParameters(parameterWithName("publicId").description("Activity public id"));
    private Snippet requestHeaders = requestHeaders(headerWithName("Authorization").description("Bearer token."));

    private FieldDescriptor[] activityDescriptorsForPost = new FieldDescriptor[] {
            fieldWithPath("name").description("The activity's name"),
            fieldWithPath("description").description("The activity's description"),
            fieldWithPath("type").description("The activity's type, one of " + Arrays.toString(ActivityType.values())),
            fieldWithPath("state").description("The current state, one of " + Arrays.toString(ActivityState.values())),
            fieldWithPath("status")
                    .description("The current status, one of " + Arrays.toString(ActivityStatus.values())),
            fieldWithPath("extraData").description("Any JSON object."),
            fieldWithPath("extraData.file_test").ignored(),
            fieldWithPath("tags").description("Array of tags."),
            fieldWithPath("media[]").description("Array of media objects"),
            fieldWithPath("media[].name").description("Name of the media resource."),
            fieldWithPath("media[].description").description("Description of the media resource."),
            fieldWithPath("media[].type")
                    .description("Media's type, one of " + Arrays.toString(com.coeux.todo.entities.MediaType.values())),
            fieldWithPath("media[].uri").description("URI pointing to media resource."),
            fieldWithPath("media[].extraData").description("Extra information related to the media resource."),
            fieldWithPath("media[].extraData.file_test").ignored(),
            fieldWithPath("labels[].description").description("Description of the label."),
            fieldWithPath("labels[].name").description("Name of the label."),
            fieldWithPath("labels[].publicId").description("ID of the label."),
    };

    private FieldDescriptor[] activityDescriptorsForGet = Stream.of(new FieldDescriptor[] {
            fieldWithPath("publicId").description("Activity public ID"),
            fieldWithPath("media[].publicId").description("ID of the Media."),
            fieldWithPath("muser.publicId").description("Id of the user.")
    }, activityDescriptorsForPost).flatMap(Stream::of)
            .toArray(FieldDescriptor[]::new);

    @MockBean
    ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    JwtTokenAuthenticationProvider authProvider;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void setUp(){
        initAuthProviderMock(MUSER);
    }

    @Test
    public void listActivitiesByUser() throws Exception {
        Activity activity = buildActivityForGet();
        List<Activity> list = Arrays.asList(activity);

        when(activityService.getActivitiesByMUser(activity.muser().publicId()))
                .thenReturn(list);

        mockMvc.perform(get("/v1/activities")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestHeaders,
                        responseFields(fieldWithPath("[]").description("An array of activities"))
                                .andWithPrefix("[].", activityDescriptorsForGet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void getActivityByPublicId() throws Exception {
        Activity activity = buildActivityForGet();
        
        when(activityService.getActivityByPublicId(activity.muser().publicId(), activity.publicId()))
                .thenReturn(activity);

        mockMvc.perform(get("/v1/activities/{publicId}", activity.publicId())
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters,
                        requestHeaders,
                        responseFields(activityDescriptorsForGet)));
    }

    @Test
    public void createActivity() throws Exception {
        Activity activity = buildActivityForGet();
        when(activityService.saveActivity(Mockito.eq(activity.muser().publicId()), Mockito.any(Activity.class)))
                .thenReturn(activity);

        mockMvc.perform(post("/v1/activities")
                .header("Authorization", "Bearer " + TOKEN)
                .content(objectMapper.writeValueAsString(buildActivityForPost(activity)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestHeaders,
                        requestFields(activityDescriptorsForPost),
                        responseFields(activityDescriptorsForGet)));
    }

    @Test
    public void deleteActivity() throws Exception {
        Activity activity = buildActivityForGet();
        mockMvc.perform(delete("/v1/activities/{publicId}", activity.publicId())
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters,
                        requestHeaders));
    }

    private Activity buildActivityForPost(Activity activity) {
        Media[] media = new Media[] { new Media(
                0, null, activity.media()[0].name(), activity.media()[0].description(),
                activity.media()[0].type(), activity.media()[0].uri(), activity.media()[0].extraInfo(),
                activity.media()[0].extraData())
        };

        return new Activity(0, null, activity.name(), activity.description(), null,
                activity.type(), activity.state(), activity.status(), activity.tags(), media,
                activity.labels(), activity.extraData());
    }

    private Activity buildActivityForGet() {
        return buildActivity(UUID.randomUUID(), UUID.randomUUID(), MUSER);
    }

    private Activity buildActivity(UUID publicId, UUID mediaPublicId, MUser muser) {
        var data = new HashMap<String, Object>();
        data.put("file_test", "Any value");
        MUser user = muser;
        Media media = new Media(0, mediaPublicId, "media",
                "Link to spring docs", com.coeux.todo.entities.MediaType.LINK,
                URI.create("https://docs.spring.io/"), null, data);
        Label label = new Label(0, UUID.randomUUID(), "RESEARCH", "Items to reserach", user);

        Activity activity = new Activity(0, publicId,
                "Research Spring docs", "Research how to document rest APIs.", user, ActivityType.LINK, ActivityState.active,
                ActivityStatus.permanent, new String[] { "1" }, new Media[] { media },
                new Label[] { label }, data);

        return activity;

    }

    public void initAuthProviderMock(MUser user) {

        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.NO_AUTHORITIES;

        User principal = new User(user.publicId().toString(), "", authorities);

        var u = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        when(authProvider.validateRequestToken(Mockito.anyString())).thenReturn(true);
        when(authProvider.getAuthentication(Mockito.anyString())).thenReturn(u);
        when(authProvider.getAuthToken(Mockito.any(HttpServletRequest.class))).thenReturn("");
        when(authProvider.isAuthTokenPresent(Mockito.any(HttpServletRequest.class))).thenReturn(true);

    }

}