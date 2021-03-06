package ba.isss.controllers;

import java.security.Principal;
import java.security.NoSuchAlgorithmException;

import ba.isss.dto.PredmetSemestarDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import ba.isss.models.Student;
import ba.isss.services.PredmetService;
import ba.isss.services.StudentService;

@RestController
@CrossOrigin
@RequestMapping(path="/student")
public class StudentController {

	@Autowired
    private StudentService studentService;
	@Autowired
	private PredmetService predmetService;

    /*** Pristup svim studentima    -  Admin
    @RequestMapping(value="/get")
    @ResponseBody
    public Student findOne(@RequestParam("id") Integer id) {
    	return studentService.findOne(id);
    }
    ***/
    // Prikaz profile-a prijavljenog studenta
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public Student getProfile(Principal principal) {
        return studentService.findByUsername(principal.getName());
    }

    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    @GetMapping(path="/buduci_predmeti")
    @ResponseBody
    public Iterable<PredmetSemestarDto> buduciPredmeti(Principal principal) {
        return predmetService.findAllFuture(studentService.findByUsername(principal.getName()).getId());
    }
    
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    @RequestMapping(value="/update_password", method = RequestMethod.POST)
    @ResponseBody
    public String updatePassword(Principal principal,@RequestParam("password1") String pass1, 
    		@RequestParam("password2") String pass2, @RequestParam("password") String pass ) throws NoSuchAlgorithmException {
    	

    	Student s = studentService.findOne(studentService.findByUsername(principal.getName()).getId());
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String oldPassHash = encoder.encodePassword(pass, null);
        String newPassHash = encoder.encodePassword(pass1, null);

        if(pass1.length() < 6)
            return "Prekratak password";
        
        if(!s.getPassword().equals(oldPassHash))
    		return "Pogresan stari password";
    	
    	if(!pass1.equals(pass2)) 
    		return "Passwordi razliciti";
    	
    	if(studentService.updatePassword(studentService.findByUsername(principal.getName()).getId(), newPassHash) == 0)
    		return "ERROR";
    	
    	return "Password promijenjen";
    }
}
