package demo;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.extend.hook.HookBus;
import org.noear.solon.extend.hook.annotation.Hook;

import java.util.HashMap;
import java.util.Map;

/**
 * @author noear
 * @since 1.8
 */
@Controller
public class DemoApp {
    public static void main(String[] args) {
        Solon.start(DemoApp.class, args);
    }

    @Hook("hello")
    @Mapping("hello")
    public String hello(String name) {
        return "Hello " + name;
    }

    @Mapping("hello2")
    public String hello2(String name) {
        Map<String,Object> args = new HashMap<>();
        HookBus.onBefore("hello", args);

        return "Hello " + name;
    }
}