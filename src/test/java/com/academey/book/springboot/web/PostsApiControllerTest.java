package com.academey.book.springboot.web;

import com.academey.book.springboot.web.domain.posts.Posts;
import com.academey.book.springboot.web.domain.posts.PostsRepository;
import com.academey.book.springboot.web.dto.PostsSaveRequestDto;
import com.academey.book.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.Null;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    @Test
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

        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(
                url,
                requestDto,
                Long.class
        );
        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
        assertThat(all.get(0).getAuthor()).isEqualTo(author);
    }

    @Test
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
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Long.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
    }

    @Test
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
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, null, Long.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        try {
            Posts deletedPosts = postsRepository.findById(deleteId).orElseGet(null);
        } catch (NullPointerException error) {
            System.out.println(111);
        }
    }
}
