package gis;

import gis.util.DetailCodeInfoParser;
import gis.util.ProxyManager;
import gis.util.StatCollector;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Main {

	public static ApplicationContext ctx;

	@Autowired
	DetailCodeInfoParser detailCodeInfoParser;
	
	@Autowired
	StatCollector statCollector;

	/**
	 * 처음 구동되는 부분.
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {

		ctx = new ClassPathXmlApplicationContext("root-context-config.xml");

		// 등록된 빈 출력
		System.out.println("################ beans");
		String[] list = ctx.getBeanDefinitionNames();
		for (String s : list) {
			System.out.println(s);
		}
		
		//파라미터
		String apikey = "key";
		if(args.length>0){
			apikey = args[0];
		}

		// 실행!
		Main executer = ctx.getBean(Main.class);
		executer.run(apikey);

	}
	
	private void run(String apikey) throws IOException, InterruptedException {
		//파싱하자!
		//detailCodeInfoParser.run();
		//프록시 설정
		ProxyManager.setProxy();
		
		while(true){
			statCollector.run(apikey);
			Thread.sleep(1000);
		}
	}
}
