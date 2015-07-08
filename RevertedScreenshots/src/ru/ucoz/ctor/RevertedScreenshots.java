package ru.ucoz.ctor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

// This class submits SEARCH_TEXT in Google Search,
// clicks on CLICK_SEARCH_RESULT in search results;
// takes screenshots for every link from footer;
// saves screenshots and log file to OUTPUT_FOLDER\<date> directory

public class RevertedScreenshots {

	public final static String GOOGLE = "http://www.google.com";
	public final static String SEARCH_FOR = "datapine.com";
	public final static String CLICK_SEARCH_RESULT = "Pricing";
	public final static String OUTPUT_FOLDER = "C:\\WebDriverScreenshots\\";
	static final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	static final Date date = new Date();

	// I have tried to break this test into a set of smaller tests
	// using @FixMethodOrder annotation, but it didn't work
	// Either new driver starts for every test
	// or the links array is not available to other tests

	@Test
	public void revertPageLinksAndTakeScreenshots() throws IOException {
		// Start FirefoxDriver, create output directory, open Google Search,
		// submit SEARCH_FOR text, click CLICK_SEARCH_RESULT
		WebDriver driver = new FirefoxDriver();
		new File(OUTPUT_FOLDER).mkdir();
		DateFormat startTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		log("FireFox has started on " + startTime.format(date));
		driver.get(GOOGLE);
		// driver.manage().window().maximize();
		driver.findElement(By.id("lst-ib")).sendKeys(SEARCH_FOR);
		driver.findElement(By.className("lsb")).click();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.findElement(By.ByLinkText.linkText(CLICK_SEARCH_RESULT)).click();
		log(driver.getCurrentUrl());
		// Receive all links on current page
		List<WebElement> elements = driver.findElements(By.xpath("//a"));
		log("\r\n" + "Total number of links: " + elements.size() + "\r\n");
		String[] links = new String[elements.size()];
		int i = 0;
		for (WebElement ele : elements) {
			links[i] = ele.getAttribute("href");
			log("Link #" + (i + 1) + " " + links[i]);
			i++;
		}
		// Revert links order
		String[] reverted_links = new String[links.length];
		int reverted_index = links.length - 1;
		for (int j = 0; j < links.length; j++) {
			reverted_links[j] = links[reverted_index];
			reverted_index--;
		}
		log("\r\n" + "Links in reverted order number: " + reverted_links.length + "\r\n");
		for (int j = 0; j < reverted_links.length; j++)
			log("Link #" + (j + 1) + " " + reverted_links[j]);
		log("\r\n"+"Taking screenshots, except for 'mailto:' links (filtered by '@' symbol):"+"\r\n");
		// Take screenshots of reverted links,
		// except for "mailto:" links, filtered by"@" symbol
		int screenshotsCounter = 0;
		for (int m = 0; m < reverted_links.length; m++) {
			if (!reverted_links[m].contains("@")) {
				screenshotsCounter++;
				driver.get(reverted_links[m]);
				driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
				File capture = ((TakesScreenshot) driver)
						.getScreenshotAs(OutputType.FILE);
				log("(screenshot " + screenshotsCounter + ")" + " "
						+ driver.getTitle());
				FileUtils.copyFile(capture,
						new File(OUTPUT_FOLDER + dateFormat.format(date) + "\\"
								+ screenshotsCounter + ".png"));
			} else {
				log("FILTERED: 'mailto:' link");
			}
		}
		log("\r\n" + "Successfully saved " + screenshotsCounter + " screenshots");
		driver.quit();
		
		SimpleDateFormat finishTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date shutdown = new Date();
		log("FireFox has shut down on " + finishTime.format(shutdown));
	}

	// This method logs test events into file "OUTPUT_FOLDER\<date>\log.txt"
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
//	Open resulting log file after the test is finished
}