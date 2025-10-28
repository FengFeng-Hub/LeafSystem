package leaf.system.api.route;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SystemRoute {
    /**
     * 后台index
     */
    @GetMapping("/backend/index")
    public String index() {
        return "/backend/index.html";
    }
    /**
     * 后台登录
     */
    @GetMapping("/backend/login")
    public String login() {
        return "/backend/login.html";
    }
}
