package attsd.exam.spring.project.controllers.webdriver.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends AbstractPage {

	private WebElement email;
	private WebElement password;
	@FindBy(id = "submit")
	private WebElement submit;
	
	
	public LoginPage(WebDriver driver) {
		super(driver);
	}
	

	public static LoginPage to(WebDriver driver) {
		get(driver, "/login");
		return PageFactory.initElements(driver, LoginPage.class);
	}

	public <T> T submitForm(Class<T> resultPage, String email, String password) {
		this.email.clear();
		this.email.sendKeys(email);
		this.password.clear();
		this.password.sendKeys(password);
		this.submit.click();
		return PageFactory.initElements(driver, resultPage);
	}
	
}