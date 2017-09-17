package ru.ucoz.ctor;

import static org.junit.Assert.assertEquals;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 Submits SEARCH_TEXT in Google Search,
 clicks on CLICK_FOUND_HEADER in search results;
 takes screenshots for every link from footer;
 saves screenshots and log file to OUTPUT_FOLDER\<date> directory
*/

public class RevertedScreenshots {
	
	private WebDriver driver;
	final static String GOOGLE = "http://www.google.com";
	final static String SEARCH_FOR = "github.com";
	final static String CLICK_FOUND_HEADER = "Join GitHub";
	final static String OUTPUT_FOLDER = "C:\\WebDriverScreenshots\\";
	static final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	static final Date date = new Date();
	private String[] links;
	int screenshotsCounter;
	List<WebElement> elements;
	File capture;
	
	By searchBox = By.id("lst-ib");
	By foundLink = By.linkText(CLICK_FOUND_HEADER);
	
	@Before
	public void setUp() throws IOException {
		driver = new FirefoxDriver();
		new File(OUTPUT_FOLDER).mkdir();
		DateFormat startTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		log("FireFox has started on " + startTime.format(date));
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	}
	
	@Test
	public void takeSearchResultScreenshots() throws IOException {
		driver.get(GOOGLE);
		driver.findElement(searchBox).sendKeys(SEARCH_FOR);
		driver.findElement(searchBox).sendKeys(Keys.ENTER);
		driver.findElement(foundLink).click();
		log(driver.getCurrentUrl());
		getPageLinks();
		takeScreenshots();
		
		assertEquals(screenshotsCounter, 24);
	}
	
	@After
	public void tearDown() throws IOException {
		SimpleDateFormat finishTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date shutdown = new Date();
		log("FireFox has shut down on " + finishTime.format(shutdown));
		// opens resulting folder to view screenshots and log
		Desktop.getDesktop().open(new File(OUTPUT_FOLDER+dateFormat.format(date)));
		driver.quit();
	}
	
	private void getPageLinks() throws IOException {
		elements = driver.findElements(By.xpath("//a"));
		log("\r\n" + "Total number of links: " + elements.size() + "\r\n");
		links = new String[elements.size()];
		int i = 0;
		for (WebElement ele : elements) {
			links[i] = ele.getAttribute("href");
			log("Link #" + (i + 1) + " " + links[i]);
			i++;
		}
	}

	// takes screenshots in reverted order
	public void takeScreenshots() throws IOException{
		screenshotsCounter = 0;
		for (int m = links.length-1; m>=0 ; m--) {
			if (!(links[m].isEmpty())) {
				screenshotsCounter++;
				driver.get(links[m]);
				try{
					capture = ((TakesScreenshot) driver)
							.getScreenshotAs(OutputType.FILE);
				} catch (WebDriverException wdex) {
					// get screenshot on error
					capture = ((TakesScreenshot) driver)
							.getScreenshotAs(OutputType.FILE);					
				}
				log("(screenshot " + screenshotsCounter + ")" + " "
						+ driver.getTitle());
				FileUtils.copyFile(capture,
						new File(OUTPUT_FOLDER + dateFormat.format(date) + "\\"
								+ screenshotsCounter + ".png"));
			} else {
				log("Filtered link");
			}
		}		
	}

	// logs test events into file "OUTPUT_FOLDER\<date>\log.txt"
	public static void log(String logtext) throws IOException {
		new File(OUTPUT_FOLDER + dateFormat.format(date)).mkdir();
		File logfile = new File(OUTPUT_FOLDER + dateFormat.format(date)+ "\\" + "log.txt");
		FileWriter fw = new FileWriter(logfile, true);
		if (logfile.length() == 0) {
			fw.write(logtext);
			fw.close();
		} else {
			fw.write("\r\n" + logtext);
			fw.close();
		}
	}
	
}