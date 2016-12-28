package uk.nhs;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.PostConstruct;

@Controller
public class PrintController {
    
    private static final Logger logger = LoggerFactory.getLogger(PrintController.class);
    
    @Value("${upload.path}")
    private String UPLOAD_PATH;

    @PostConstruct
    public void init() {
        logger.info("Upload path: {}", UPLOAD_PATH);
    }
    
    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "print service";
    }
    
    @RequestMapping(method=RequestMethod.POST, value="/add", consumes = "multipart/form-data")
    @ResponseBody
    public String upload(@RequestParam String fileId,
                         @RequestParam("files") MultipartFile[] files) {
        
        logger.info("upload file request: {}", fileId);
        
        UPLOAD_PATH += UPLOAD_PATH.endsWith("/") ? "" : "/";
         
        Arrays.stream(files).forEach(file -> {
            if (!file.isEmpty()) {
                File fileDest = getDestinationFile(fileId, file);
                
                try {
                    file.transferTo(fileDest);
                    logger.info("Uploaded file: {}", fileDest);
                }
                catch(Exception e) {
                    logger.warn("Exception writing file: {}", fileDest, e);
                }
            }
        });
        
        // TODO redirect to success page
        return "success";
    }
    
    @RequestMapping("/list")
    @ResponseBody
    public String list() {
        return Arrays.asList(new File(UPLOAD_PATH).list()).toString();
    }
    
    File getDestinationFile(String fileId, MultipartFile file) {
        String fileName = getFileName(fileId, file);
        return new File(UPLOAD_PATH + fileName.replace("/",""));
    }
    
    String getFileName(String fileId, MultipartFile file) {
        return new StringBuilder()
            .append(fileId)
            .append(new Date().getTime())
            .append(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")))
            .toString().trim();
    }
}
