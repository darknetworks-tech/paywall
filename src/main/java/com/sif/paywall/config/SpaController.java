package com.sif.paywall.config;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class SpaController {
        @RequestMapping(value = { "/", "/{x:[\\w\\-]+}", "/**/{x:[\\w\\-]+}" })
        public String forward() {
            return "forward:/index.html";
        }


}
