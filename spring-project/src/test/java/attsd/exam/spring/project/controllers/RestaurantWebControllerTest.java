package attsd.exam.spring.project.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;

import attsd.exam.spring.project.model.Restaurant;
import attsd.exam.spring.project.model.RestaurantDTO;
import attsd.exam.spring.project.services.RestaurantService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RestaurantWebController.class)
public class RestaurantWebControllerTest {


	@Autowired
	private MockMvc mvc;

	@MockBean
	private RestaurantService restaurantService;

	@Test
	@WithMockUser
	public void testGetIndex() throws Exception {
		mvc.perform(get("/")).andExpect(view().name("index")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void testStatus200() throws Exception {
		mvc.perform(get("/")).andExpect(status().is2xxSuccessful());
	}

	@Test
	@WithMockUser
	public void testReturnHomeView() throws Exception {
		ModelAndViewAssert.assertViewName(mvc.perform(get("/")).andReturn().getModelAndView(), "index");
	}

	@Test
	@WithMockUser
	public void testEmptyRestaurantList() throws Exception {
		mvc.perform(get("/")).andExpect(view().name("index"))
				.andExpect(model().attribute("restaurants", new ArrayList<Restaurant>()))
				.andExpect(model().attribute("message", "No restaurants"));
		verify(restaurantService).getAllRestaurants();
	}

	@Test
	@WithMockUser
	public void testNotEmptyRestaurantList() throws Exception {
		List<Restaurant> restaurants = Arrays.asList(new Restaurant(BigInteger.valueOf(1), "DaAndrea", 30));
		when(restaurantService.getAllRestaurants()).thenReturn(restaurants);
		mvc.perform(get("/")).andExpect(view().name("index")).andExpect(model().attribute("restaurants", restaurants))
				.andExpect(model().attribute("message", ""));
		verify(restaurantService).getAllRestaurants();
	}

	@Test
	@WithMockUser
	public void testSingleRestaurant() throws Exception {
		Restaurant restaurant = new Restaurant(BigInteger.valueOf(1), "Zorba", 23);
		when(restaurantService.getRestaurantById(BigInteger.valueOf(1))).thenReturn(restaurant);
		String id = "1";
		mvc.perform(get("/edit?id="+id)).andExpect(view().name("edit"))
				.andExpect(model().attribute("restaurant", restaurant)).andExpect(model().attribute("message", "Edit restaurant"));
		verify(restaurantService).getRestaurantById(BigInteger.valueOf(1));
	}

	@Test
	@WithMockUser
	public void testSingleRestaurantNotFound() throws Exception {
		String id = "1";
		mvc.perform(get("/edit?id="+id)).andExpect(view().name("error"));
		verify(restaurantService).getRestaurantById(BigInteger.valueOf(1));
	}

	@Test
	@WithMockUser
	public void testPostRestaurant() throws Exception {
		RestaurantDTO restaurantDTO = new RestaurantDTO(null, "LaFiaccola", 45);
		mvc.perform(post("/save").param("name", restaurantDTO.getName())
				.param("averagePrice", "" + restaurantDTO.getAveragePrice())).andExpect(view().name("redirect:/"));
		Restaurant r = new Restaurant();
		r.setId(restaurantDTO.getId());
		r.setName(restaurantDTO.getName());
		r.setAveragePrice(restaurantDTO.getAveragePrice());
		verify(restaurantService, times(1)).storeInDb(r);
	}

	@Test
	@WithMockUser
	public void testNewRestaurant() throws Exception {
		mvc.perform(get("/new")).andExpect(view().name("edit"))
				.andExpect(model().attribute("restaurant", new Restaurant(null, null, 0)))
				.andExpect(model().attribute("message", ""));
		verifyZeroInteractions(restaurantService);
	}

	@Test
	@WithMockUser
	public void testDeleteRestaurantWhenNotExists() throws Exception {
		Restaurant r = new Restaurant();
		String id = "1";
		mvc.perform(get("/delete?id="+id)).andExpect(view().name("error"));
		verify(restaurantService, times(1)).getRestaurantById(BigInteger.valueOf(1));
		verify(restaurantService, times(0)).delete(r);
	}
	
	@Test
	@WithMockUser
	public void testDeleteRestaurant() throws Exception {
		Restaurant r = new Restaurant(BigInteger.valueOf(1), "CacioEPepe", 30);
		when(restaurantService.getRestaurantById(BigInteger.valueOf(1))).thenReturn(r);
		String id = "1";
		mvc.perform(get("/delete?id="+id)).andExpect(view().name("redirect:/"));
		verify(restaurantService, times(1)).getRestaurantById(BigInteger.valueOf(1));
		verify(restaurantService, times(1)).delete(r);
	}

	@Test
	@WithMockUser
	public void testResetRestaurants() throws Exception {
		mvc.perform(get("/reset")).andExpect(view().name("redirect:/"));
		verify(restaurantService).deleteAll();
	}

}
