package hexlet.code.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForwardController {

    @GetMapping(value = {"/{path:^(?!api|h2-console|welcome|assets|docs|v3|index\\.html$).*}/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
