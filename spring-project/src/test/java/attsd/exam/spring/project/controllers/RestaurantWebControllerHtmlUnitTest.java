package attsd.exam.spring.project.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

import attsd.exam.spring.project.model.Restaurant;
import attsd.exam.spring.project.services.RestaurantService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RestaurantWebController.class, secure = false)
public class RestaurantWebControllerHtmlUnitTest {

	
	@Autowired
	private WebClient webClient;

	@MockBean
	private RestaurantService restaurantService;
	
	private MockMvc mvc;
	
	@Autowired
	private WebApplicationContext context;
	
	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}
	
	@After
	public void clearAll() {
		restaurantService.deleteAll();
	}

	@Test
	@WithMockUser("username")
	public void testHomePageWithRestaurants() throws Exception {
		mvc.perform(get("/").sessionAttr("user", "username").with(csrf()))
		.andExpect(status().isOk());
		when(restaurantService.getAllRestaurants())
				.thenReturn(Arrays.asList(new Restaurant(BigInteger.valueOf(1), "SaleGrosso", 35),
						new Restaurant(BigInteger.valueOf(2), "Scaraboci", 40)));
		HtmlPage page = this.webClient.getPage("/");
		assertThat(page.getTitleText()).isEqualTo("Restaurants");
		HtmlTable table = page.getHtmlElementById("restaurant_table");
		assertThat(page.getBody().getTextContent()).doesNotContain("No restaurant");
		assertThat(table.asText())
				.isEqualTo("ID	Name	AveragePrice\n" + "1	SaleGrosso	35\n" + "2	Scaraboci	40");
	}
	
	@Test
	public void testHomePageWithNoRestaurants() throws Exception {	
		HtmlPage page = this.webClient.getPage("/");
		assertThat(page.getTitleText()).isEqualTo("Restaurants");
		assertThat(page.getBody().getTextContent()).contains("No restaurant");
	}

	@Test
	public void testEditNonExistentRestaurant() throws Exception {
		HtmlPage page = this.webClient.getPage("/edit/1");
		assertThat(page.getBody().getTextContent()).contains("No restaurant found with id: 1");
	}

	@Test
	public void testEditExistentRestaurant() throws Exception {
		when(restaurantService.getRestaurantById(BigInteger.valueOf(1)))
				.thenReturn(new Restaurant(BigInteger.valueOf(1), "IlGrammofono", 15));
		HtmlPage page = this.webClient.getPage("/edit/1");
		assertThat(page.getBody().getTextContent()).doesNotContain("No restaurant found with id: 1");
		final HtmlForm form = page.getFormByName("restaurant_form");
		form.getInputByValue("IlGrammofono").setValueAttribute("CapoNord");
		form.getInputByValue("15").setValueAttribute("20");
		Restaurant expectedSave = new Restaurant(BigInteger.valueOf(1), "CapoNord", 20);
		when(restaurantService.getAllRestaurants()).thenReturn(Arrays.asList(expectedSave));
		final HtmlButton button = form.getButtonByName("btn_submit");
		final HtmlPage page2 = button.click();
		verify(restaurantService).storeInDb(expectedSave);
		HtmlTable table = page2.getHtmlElementById("restaurant_table");
		assertThat(table.asText()).isEqualTo("ID	Name	AveragePrice\n" + "1	CapoNord	20");
	}

	@Test
	public void testNewRestaurant() throws Exception {
		HtmlPage page = this.webClient.getPage("/new");

		final HtmlForm form = page.getFormByName("restaurant_form");

		form.getInputByName("id").setValueAttribute("1");
		form.getInputByName("name").setValueAttribute("BorgoAlCotone");
		form.getInputByName("averagePrice").setValueAttribute("40");

		Restaurant expectedSave = new Restaurant(BigInteger.valueOf(1), "BorgoAlCotone", 40);

		when(restaurantService.getAllRestaurants()).thenReturn(Arrays.asList(expectedSave));

		final HtmlButton button = form.getButtonByName("btn_submit");
		final HtmlPage page2 = button.click();

		verify(restaurantService).storeInDb(expectedSave);
		HtmlTable table = page2.getHtmlElementById("restaurant_table");

		assertThat(table.asText()).isEqualTo("ID	Name	AveragePrice\n" + "1	BorgoAlCotone	40");
	}
	
	
}