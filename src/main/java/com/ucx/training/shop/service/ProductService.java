package com.ucx.training.shop.service;

import com.ucx.training.shop.entity.Brand;
import com.ucx.training.shop.entity.FileUpload;
import com.ucx.training.shop.entity.Platform;
import com.ucx.training.shop.entity.Product;
import com.ucx.training.shop.exception.NotFoundException;
import com.ucx.training.shop.repository.FileUploadRepository;
import com.ucx.training.shop.repository.ProductRepository;
import com.ucx.training.shop.util.FileUploadUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@Transactional
@Log4j2
public class ProductService extends BaseService<Product, Integer> {

    private ProductRepository productRepository;
    private PlatformService platformService;
    private BrandService brandService;
    @Value("${file.upload}")
    private String uploadDirName;

    @Value("${file.upload.client}")
    private String clientDirName;
    private FileUploadRepository fileRepository;
    private FileUploadService fileUploadService;

    public ProductService(ProductRepository productRepository, PlatformService platformService, BrandService brandService, FileUploadRepository fileRepository, FileUploadService fileUploadService) {
        this.productRepository = productRepository;
        this.platformService = platformService;
        this.brandService = brandService;
        this.fileRepository = fileRepository;
        this.fileUploadService = fileUploadService;
    }

    public Product findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Null argument provided!");
        }

        return productRepository.findByName(name);
    }

    public List<Product> findAllByUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("Null argument provided!");
        }

        return productRepository.findAllByUnitPrice(unitPrice);
    }

    public FileUpload uploadFile(MultipartFile file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        String directoryPathAsString = System.getProperty("user.dir") + uploadDirName;
        Path directoryPath = Paths.get(directoryPathAsString);
        if (!Files.exists(directoryPath)) {
            Files.createDirectory(directoryPath);
        }
        String filePathAsString = directoryPathAsString + file.getOriginalFilename();
        String clientSideFilePathAsString = clientDirName + file.getOriginalFilename();
        Path filePath = Paths.get(filePathAsString);
        Path clientSideFilePath = Paths.get(clientSideFilePathAsString);

        byte[] bytes = file.getBytes();
        Files.write(filePath, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Files.write(clientSideFilePath, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        return FileUpload.builder()
                .fileName(file.getOriginalFilename())
                .filePath(filePathAsString)
                .fileExtension(FileUploadUtil.getFileExtension(file.getOriginalFilename()))
                .build();
    }

    public FileUpload saveFile(FileUpload uploadedFile, Integer productId) throws NotFoundException {
        if (uploadedFile == null || productId == null) {
            throw new IllegalArgumentException("Product id must exist");
        }
        Product foundProduct = this.findById(productId);
        if (foundProduct == null) {
            throw new NotFoundException("Product could not be found" + productId);
        }
        FileUpload foundFileUpload = fileUploadService.findByProductAndRecordStatusActive(foundProduct);
        if (foundFileUpload == null) {
            uploadedFile.setProduct(foundProduct);
            return fileUploadService.save(uploadedFile);
        }
        return fileUploadService.update(uploadedFile, foundFileUpload.getId());
    }

    public Product createProductWithPlatformAndBrand(Product product) throws NotFoundException {
        if (product == null) {
            throw new IllegalArgumentException("Given product is null");
        }
        if (product.getId() != null) {
            Product foundProduct = super.findById(product.getId());
            if (foundProduct == null) {
                throw new RuntimeException("The given id for the product is invalid");
            } else {
                return super.update(product, foundProduct.getId());
            }
        }
        Platform platform = product.getPlatform();
        Brand brand = product.getBrand();
        /*if (platform != null && platform.getId() == null) {
            platformService.save(platform);
        } else {
            Platform foundPlatform = platformService.findById(platform.getId());
            Assert.isTrue(foundPlatform != null, "Entity not found!");
            product.setPlatform(foundPlatform);
        }

        if (brand.getId() == null) {
            brandService.save(brand);
        } else {
            Brand foundBrand = brandService.findById(brand.getId());
            Assert.isTrue(foundBrand != null, "Entity not found!");
            product.setBrand(foundBrand);
        }*/
        return super.save(product);
    }

    public List<Product> findAllActive() {
        return productRepository.findAllActive();
    }

    @org.springframework.transaction.annotation.Transactional
    public Product createProductWithImage(Product product, MultipartFile file) throws IOException, NotFoundException {
        Product createdProduct = createProductWithPlatformAndBrand(product);
        FileUpload uploadedFile = uploadFile(file);
        FileUpload fileWithProduct = saveFile(uploadedFile, createdProduct.getId());
        createdProduct.setFileUpload(fileWithProduct);
        return createdProduct;
    }
}
