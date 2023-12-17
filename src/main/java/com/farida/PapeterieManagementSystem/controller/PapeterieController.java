package com.farida.PapeterieManagementSystem.controller;



import com.farida.PapeterieManagementSystem.model.Papeterie;
import com.farida.PapeterieManagementSystem.model.User;
import com.farida.PapeterieManagementSystem.service.PapeterieService;
import com.farida.PapeterieManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
@Controller
@RequestMapping("/papeteries")
public class PapeterieController {

    private Logger logger = Logger.getLogger(getClass().getName());

    private UserService userService;

    private PapeterieService papeterieService;




    @Autowired
    public PapeterieController(UserService userService, PapeterieService papeterieService) {
        this.userService = userService;
        this.papeterieService = papeterieService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder)
    {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    private static String UPLOAD_DIR = "./uploads/";
    private static String IMAGE_DIR = "./images/";

    @PostMapping("/save")
    public String saveJob(@RequestParam("report") MultipartFile report, @RequestParam("pic") MultipartFile pic,
                          @Valid @ModelAttribute("papeterie") Papeterie thePapeterie, User usr,
                          BindingResult theBindingResult, @RequestParam("AuthId") int AuthId,
                          HttpSession session, HttpServletRequest request) {


        logger.warning("=========Init binder================");
        if(theBindingResult.hasErrors()){
            return "papeterie";
        }


        Papeterie papeterie = new Papeterie();

        logger.warning("==============Author==============");
        Long userId = (long) AuthId;
        User existingUser = userService.findById(userId);
        logger.warning("\n " + existingUser.toString()+"\n");

        // now Add the userId to the session of future reference in the session
        session = request.getSession();
        if (session.getAttribute("AuthId") == null) {
            session.setAttribute("AuthId", userId);
            logger.warning("================AuthId set as session attribute============");
        }
        logger.warning("========Job========");
        logger.warning(thePapeterie.toString());

        Papeterie dbPapeterie = new Papeterie(thePapeterie.getName(), thePapeterie.getDescription(), thePapeterie.getPrice(), thePapeterie.getQuantity(), thePapeterie.getPurchaseDate(), thePapeterie.getCategory());
        //saving file to a server
        try {
            byte[] bytes = report.getBytes();
            String originalFileName = report.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + originalFileName);

            if (Files.exists(path)) {
                // File with the same name already exists, do not save
                logger.info("File with name " + originalFileName + " already exists, skipping save.");
                dbPapeterie.setSpecification(originalFileName);
            } else {
                // Save the file
                Files.write(path, bytes);
                dbPapeterie.setSpecification(originalFileName);
                logger.info(">>>>>>>>>>>>>>>>>>>>>>Path for file " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            byte[] bytes = pic.getBytes();
            String originalPicName = pic.getOriginalFilename();
            Path filePath = Paths.get(IMAGE_DIR + originalPicName);
            if (Files.notExists(filePath)) {
                Files.write(filePath, bytes);
                dbPapeterie.setItemImage(originalPicName);
                logger.info(">>>>>>>>>>>>>>>>>>>>>Path for Image" + filePath);
            } else {
                dbPapeterie.setItemImage(originalPicName);
                logger.info(">>>>>>>>>>>>>>>>>>>>>Image already exists: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        List<Integer> all =userService.findAllIds();
//        System.out.println("available user" + all);

//        System.out.println("assigning user to the job");
        String userN = usr.getUsername();
        logger.info("Processing username for: " + userN);
        if(theBindingResult.hasErrors()){
            System.out.println("Username not found");
        }


            papeterieService.save(dbPapeterie, userId);


        return "success-job";
    }

    @GetMapping("/showFormForAdd")
    public String showFormForAdd(Model theModel, HttpSession session) {


        //create model atrribute to bind form data
        Papeterie thePapeterie = new Papeterie();
        theModel.addAttribute("papeterie", thePapeterie);
        // get the userRoles list from session and add it to the model
        List<String> userRoles = (List<String>) session.getAttribute("roles");
        User theUser = (User) session.getAttribute("user");
        if((userRoles==null) || theUser == null){
            //response.sendRedirect(request.getContextPath() + "/");
            logger.info("the credentials are null");
            return "redirect:/";
        }

        logger.info(">>>>>>>>>>Current User = " + theUser.getFirstName());
        for(String role:userRoles){
            logger.info(">>>>>>>>>>role = " + role);
        }
        theModel.addAttribute("AuthId", theUser.getId());
        theModel.addAttribute("theUser", theUser);
        return "papeterie";

    }

    @GetMapping("/list")
    public String listEmployees(Model theModel) {

        String keyword = null;

        return listByPage(theModel, 1, keyword);
    }

    @GetMapping("/page/{pageNumber}")
    public String listByPage(Model theModel, @PathVariable("pageNumber") int currentPage,
                             @Param("keyword") String keyword) {


        //pagination
        Page<Papeterie> page = papeterieService.findAll(currentPage, keyword);
        long totalItems = page.getTotalElements();
        int totalPages = page.getTotalPages();
        // get jobs from DB
        List<Papeterie> thePapeteries = page.getContent();
//        //get user
//        List<Integer> users = userService.findAllIds();
        //add to the spring model
//        theModel.addAttribute("users", users);
        theModel.addAttribute("currentPage", currentPage);
        theModel.addAttribute("totalItems", totalItems);
        theModel.addAttribute("totalPages", totalPages);
        theModel.addAttribute("thePapeteries", thePapeteries);
        theModel.addAttribute("keyword", keyword);

        return "papeterie-list";

    }

    @GetMapping("/view/{filename:.+}")
    public void viewFile(@PathVariable String filename, HttpServletResponse response) throws IOException {
        Path file = Paths.get(UPLOAD_DIR + filename);
        logger.info(">>>>>>>>>>>>>>>>>>>>>>Name of file at download " + filename);
        if (Files.exists(file)) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=" + filename);
            Files.copy(file, response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = new FileSystemResource(IMAGE_DIR + filename);
        if (file.exists() && file.isReadable()) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(file);
        } else {
            return ResponseEntity.notFound().build();
        }


    }

    @PostMapping("/showFormForUpdate")
    public String updateJob(@RequestParam("papeterieId") Long theId, Model theModel, HttpServletRequest request) {

        //get the job from service
        Papeterie thePapeteries = papeterieService.findById(theId);
        //set job in the model to prepolulate the form
        theModel.addAttribute("papeterie", thePapeteries);

        HttpSession session = request.getSession();
        theModel.addAttribute("AuthId", (Long)session.getAttribute("AuthId"));
        //send over to our form
        return "papeterie";
    }
    @PostMapping("/deletePapeterie")
    public String deleteJob(@RequestParam("papeterieId") Long theId){
        //delete the job
        papeterieService.deleteById(theId);
        //redirect to the list
        return "redirect:/papeteries/list";
    }
}


