package com.academey.book.springboot.web;

import com.academey.book.springboot.web.domain.posts.Posts;
import com.academey.book.springboot.web.domain.posts.PostsRepository;
import com.academey.book.springboot.web.dto.PostsSaveRequestDto;
import com.academey.book.springboot.web.dto.PostsUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.validation.constraints.Null;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// WebMvcTest 는 JAP 기능이 작동 안한다. 오직 Controller 같은 빈만 사용된다.
// 따라서 JPA 가 필요하면 SpringBootTest 랑 TestRestTemplate 을 쓰거나,
// 아래 방식처럼 MockMvc 를 써서 구현하면 된다.
// 근데 또 WithMockUser 같은 어노테이션을 쓰려면 MockMvc 를 사용해야 한다.
// SpringBootTest 에서 MockMvc 를 사용하려면 다음과 같이 setUp 을 해주면 된다.
public class PostsApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_등록된다() throws Exception {
        // given
        String title = "title";
        String content = "content";
        String author = "author";
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        String url = "http://localhost:" + port + "/api/v1/posts";

        // when
        mvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(
                                new ObjectMapper().writeValueAsString(requestDto))
        ).andExpect(
                status().isOk()
        );

        // then

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
        assertThat(all.get(0).getAuthor()).isEqualTo(author);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_수정된다() throws Exception {
        // given
        String title = "title";
        String content = "content";
        String author = "author";
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title(title)
                .author(author)
                .content(content)
                .build()
        );

        Long updateId = savedPosts.getId();
        String expectedTitle = "title2";
        String expectedContent = "content2";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                .title(expectedTitle)
                .content(expectedContent)
                .build();
        String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        HttpEntity<PostsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

        //when
        mvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(
                                new ObjectMapper().writeValueAsString(requestDto))
        ).andExpect(
                status().isOk()
        );

        //then
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_삭제된다() throws Exception {
        // given
        String title = "title";
        String content = "content";
        String author = "author";
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title(title)
                .author(author)
                .content(content)
                .build()
        );

        Long deleteId = savedPosts.getId();

        String url = "http://localhost:" + port + "/api/v1/posts/" + deleteId;


        //when
        mvc.perform(
                delete(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        ).andExpect(
                status().isOk()
        );


        //then
        try {
            Posts deletedPosts = postsRepository.findById(deleteId).orElseGet(null);
        } catch (NullPointerException error) {
            System.out.println(111);
        }
    }
}
