package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.Param;
import framework.annotation.PostMapping;
import framework.utilitaire.ModelAndView;
import framework.utilitaire.FileStorageService;
import framework.utilitaire.UploadedFile;

@Controller
public class PhotoController {

    // Répertoire d'upload défini dans le contrôleur (basé sur catalina.base de Tomcat)
    // Exemple de résultat: D:/xampp/tomcat/uploads/photos
    private static final String UPLOAD_DIR = System.getProperty("catalina.base") + "/uploads/photos";

    @GetMapping("/upload")
    public ModelAndView form() {
        return new ModelAndView("/upload.jsp");
    }

    @PostMapping("/upload")
    public ModelAndView handle(@Param("title") String title,
                               @Param("photo") UploadedFile photo) {
        // Si aucun fichier n'est sélectionné
        if (photo == null || photo.getSize() == 0) {
            ModelAndView mv = new ModelAndView("/upload.jsp");
            mv.addObject("title", title);
            mv.addObject("error", "Aucun fichier reçu. Veuillez choisir un fichier.");
            return mv;
        }

        // Stocker le fichier dans le dossier fixé par le contrôleur
        String stored = FileStorageService.storeIn(photo, UPLOAD_DIR);

        ModelAndView mv = new ModelAndView("/upload.jsp");
        mv.addObject("title", title);
        mv.addObject("stored", stored);
        mv.addObject("original", photo != null ? photo.getOriginalFilename() : null);
        mv.addObject("size", photo != null ? photo.getSize() : null);
        return mv;
    }
}
