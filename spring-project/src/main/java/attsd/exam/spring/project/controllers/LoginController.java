package attsd.exam.spring.project.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import attsd.exam.spring.project.model.User;
import attsd.exam.spring.project.model.UserDTO;
import attsd.exam.spring.project.services.UserService;

@Controller
public class LoginController {

	@Autowired
	private UserService userService;

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/signup")
	public String signup() {
		return "signup";
	}
	
	@PostMapping("/signup")
	public String createNewUser(@Valid UserDTO user) {
		User userExists = userService.findUserByEmail(user.getEmail());
		if (userExists != null) {
			return "error";
		}
		
		else {
			User realUser = new User();
			realUser.setEmail(user.getEmail());
			realUser.setPassword(user.getPassword());
			realUser.setUsername(user.getUsername());
			userService.saveUser(realUser);
			return "login";
		}
	}


}