package com.visionvera.web.controller.rest;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.service.TrackService;
import com.visionvera.service.UserService;
import com.visionvera.vo.Track;
import com.visionvera.vo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController extends BaseReturn {

    @Resource
    UserService userService;

    @Resource
    TrackService trackService;


    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public String querAll(HttpServletRequest request,
                              @RequestParam(value = "email",required = false) String email) {
        User user = new User();
        try {
            String ipAddress = user.getIpAddress(request);

            Track ip = trackService.findIp(ipAddress);
            Track track = new Track();
            if (null == ip) {
                track.setIp(ipAddress);
                track.setVersion(1);
                track.setCreateDate(new Date());
                track.setCountry(email);
                trackService.add(track);
            } else {
                track.setVersion(ip.getVersion() + 1);
                track.setModifyDate(new Date());
                track.setCountry(email);
                track.setId(ip.getId());
                trackService.update(track);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "erroe";
        }
        return "success";

    }



}
