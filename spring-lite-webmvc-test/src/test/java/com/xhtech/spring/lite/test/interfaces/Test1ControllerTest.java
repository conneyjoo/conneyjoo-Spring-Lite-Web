package com.xhtech.spring.lite.test.interfaces;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class Test1ControllerTest
{
    @Autowired
    protected TestRestTemplate restTemplate;

    @Test
    public void getBoxServerUserIdDownload()
    {
        String result = restTemplate.getForObject("/test1/box/server/123/download?url=aa", String.class);
        Assert.isTrue(result.startsWith("123"), result);
    }

    @Test
    public void getBoxServerADownload()
    {
        String result = restTemplate.getForObject("/test1/box/server/*/download1", String.class);
        Assert.isTrue(result.startsWith("*"), result);
    }

    @Test
    public void getBoxServerAUserId()
    {
        String result = restTemplate.getForObject("/test1/box/server/*/123", String.class);
        Assert.isTrue(result.startsWith("123"), result);
    }

    @Test
    public void getBoxServerAppIdUserId()
    {
        String result = restTemplate.getForObject("/test1/box/server/10/10", String.class);
        Assert.isTrue(result.startsWith("20"), result);
    }

    @Test
    public void getBoxServerDownload()
    {
        String result = restTemplate.getForObject("/test1/box/server/download", String.class);
        Assert.isTrue(result.startsWith("3"), result);
    }

    @Test
    public void postBoxServerDownload()
    {
        String result = restTemplate.postForObject("/test1/box/server/download", "", String.class);
        Assert.isTrue(result.startsWith("3a"), result);
    }

    @Test
    public void getBoxServerValue()
    {
        String result = restTemplate.getForObject("/test1/box/server/tierd", String.class);
        Assert.isTrue(result.startsWith("4"), result);
    }

    @Test
    public void getBoxServerAAFileDownload()
    {
        String result = restTemplate.getForObject("/test1/box/server/11/22/33/44/file/download", String.class);
        Assert.isTrue(result.startsWith("5"), result);
    }

    @Test
    public void getBoxServerAAFileDownloadTest()
    {
        String result = restTemplate.getForObject("/test1/box/server/11/22/33/44/file/download/test", String.class);
        Assert.isTrue(result.startsWith("6"), result);
    }

    @Test
    public void getBoxServerAAFileDownloadUserId()
    {
        String result = restTemplate.getForObject("/test1/box/server/11/22/33/44/file/download/123", String.class);
        Assert.isTrue(result.startsWith("123"), result);
    }

    @Test
    public void getBoxServeraaaFileAADownloadTest()
    {
        String result = restTemplate.getForObject("/test1/box/server/aaa/file/112321/dsa123/dsa/download/test", String.class);
        Assert.isTrue(result.startsWith("7"), result);
    }

    @Test
    public void getBoxServeraaaFileAA()
    {
        String result = restTemplate.getForObject("/test1/box/server/aaa/file/112321/dsa123/dsa/zzz", String.class);
        Assert.isTrue(result.startsWith("8"), result);
    }

    @Test
    public void getBoxServerUserIdSchoolIdAppId()
    {
        String result = restTemplate.getForObject("/test1/box/server/1/2/3", String.class);
        Assert.isTrue(result.startsWith("9"), result);
    }

    @Test
    public void getBoxServerASchoolIdAppId()
    {
        String result = restTemplate.getForObject("/test1/box/server/kaka/1/dada/2", String.class);
        Assert.isTrue(result.startsWith("10"), result);
    }

    @Test
    public void boxServerUserIdRequest()
    {
        String result = restTemplate.getForObject("/test1/box/server/1/request?url=aa", String.class);
        Assert.isTrue(result.startsWith("aa"), result);

        result = restTemplate.postForObject("/test1/box/server/2/request?url=bb", "", String.class);
        Assert.isTrue(result.startsWith("bb"), result);
    }
}
