package gis;

import gis.dump.CodeInfoImporter;
import gis.dump.DataImporter;
import gis.trans.CntCalculator;
import gis.trans.CoordArrayConverter;
import gis.trans.DistCalculator;
import gis.trans.PositionCalculator;
import gis.util.CompCollector;
import gis.util.DetailCodeInfoParser;
import gis.util.StatCollector;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	@Autowired
	CompCollector compCollector;

	@Autowired
	@Qualifier("dataImport")
	DataImporter dataImporter;

	@Autowired
	PositionCalculator positionCalculator;

	@Autowired
	DistCalculator distCalculator;

	@Autowired
	CntCalculator cntCalculator;

	@Autowired
	CodeInfoImporter codeInfoImporter;
	
	@Autowired
	CoordArrayConverter coordArrayConverter;

	/**
	 * 처음 구동되는 부분.
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException, ParseException {

		ctx = new ClassPathXmlApplicationContext("root-context-config.xml");

		// 등록된 빈 출력
		System.out.println("################ beans");
		String[] list = ctx.getBeanDefinitionNames();
		for (String s : list) {
			System.out.println(s);
		}

		// 파라미터
		String apikey = "key";
		if (args.length > 0) {
			apikey = args[0];
		}

		// 실행!
		Main executer = ctx.getBean(Main.class);
		executer.run(apikey);

	}

	private void run(String apikey) throws IOException, InterruptedException,
			ParseException {
		// 파싱하자!
		// detailCodeInfoParser.run();
		// 프록시 설정
		// ProxyManager.setProxy();
		// compCollector.test();

		/*
		 * while(true){ statCollector.run(apikey); Thread.sleep(1000); }
		 */

		// 데이터 임포트
		// dataImporter.run();

		// 흐흐
		// positionCalculator.run();

		// 최단거리 계산
		// distCalculator.run();

		// 일정거리 안에 있는 개수 계산
		//cntCalculator.run();

		// 코드정보 입력.
		//codeInfoImporter.run();
		
		//좌표 변환
		coordArrayConverter.run();
	}
}
