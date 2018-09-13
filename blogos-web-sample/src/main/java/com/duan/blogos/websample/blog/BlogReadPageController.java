package com.duan.blogos.websample.blog;

import com.duan.blogos.service.dto.blog.BlogMainContentDTO;
import com.duan.blogos.service.dto.blog.BlogStatisticsCountDTO;
import com.duan.blogos.service.dto.blogger.BloggerAccountDTO;
import com.duan.blogos.service.dto.blogger.BloggerStatisticsDTO;
import com.duan.blogos.service.exception.CodeMessage;
import com.duan.blogos.service.properties.BloggerProperties;
import com.duan.blogos.service.restful.ResultBean;
import com.duan.blogos.service.service.audience.BlogBrowseService;
import com.duan.blogos.service.service.blogger.*;
import com.duan.blogos.service.service.common.BlogStatisticsService;
import com.duan.blogos.service.service.common.OnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created on 2018/3/7.
 *
 * @author DuanJiaNing
 */
@Controller
@RequestMapping("/{bloggerName}/blog/{blogName}")
public class BlogReadPageController {

    @Autowired
    private BloggerAccountService accountService;

    @Autowired
    private BloggerBlogService blogService;

    @Autowired
    private BlogStatisticsService statisticsService;

    @Autowired
    private BloggerStatisticsService bloggerStatisticsService;

    @Autowired
    private BloggerProperties bloggerProperties;

    @Autowired
    private OnlineService onlineService;

    @Autowired
    private BlogBrowseService blogBrowseService;

    @Autowired
    private BloggerLikeBlogService likeService;

    @Autowired
    private BloggerCollectBlogService collectBlogService;

    @RequestMapping
    public ModelAndView page(HttpServletRequest request,
                             @PathVariable String bloggerName,
                             @PathVariable String blogName) {
        ModelAndView mv = new ModelAndView();

        // 博文作者博主账户
        BloggerAccountDTO account = accountService.getAccount(bloggerName);

        if (account == null) {
            request.setAttribute("code", CodeMessage.BLOGGER_UNKNOWN_BLOGGER.getCode());
            mv.setViewName("/blogger/register");
            return mv;
        }

        int blogId = blogService.getBlogId(account.getId(), blogName);
        if (blogId == -1) {
            mv.setViewName("error/error");
            mv.addObject("code", 5);
            mv.addObject(bloggerProperties.getSessionNameOfErrorMsg(), "博文不存在！");
            return mv;
        }

        // 博文浏览次数自增1
        statisticsService.updateBlogViewCountPlus(blogId);

        ResultBean<BlogMainContentDTO> mainContent = blogBrowseService.getBlogMainContent(blogId);
        ResultBean<BlogStatisticsCountDTO> statistics = statisticsService.getBlogStatisticsCount(blogId);

        mv.addObject("blogOwnerBloggerId", account.getId());
        mv.addObject("main", mainContent.getData());
        mv.addObject("stat", statistics.getData());

        // 登陆博主 id
        String token = ""; // TODO redis + token 维护会话
        int loginBloggerId = onlineService.getLoginBloggerId(token);

        ResultBean<BloggerStatisticsDTO> loginBgStat = bloggerStatisticsService.getBloggerStatistics(loginBloggerId);
        mv.addObject("loginBgStat", loginBgStat.getData());

        if (loginBloggerId != -1) {
            if (likeService.getLikeState(loginBloggerId, blogId))
                mv.addObject("likeState", true);
            if (collectBlogService.getCollectState(loginBloggerId, blogId))
                mv.addObject("collectState", true);
        }

        mv.setViewName("blogger/read_blog");
        return mv;
    }

}
