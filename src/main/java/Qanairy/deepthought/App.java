package Qanairy.deepthought;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.minion","com.qanairy"})
@PropertySources({
	@PropertySource("classpath:application.properties")
})
public class App 
{
    public static void main( String[] args )
    {
    	 SpringApplication.run(App.class, args);
	 }
}
