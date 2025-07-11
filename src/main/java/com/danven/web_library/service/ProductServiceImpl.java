package com.danven.web_library.service;

import com.danven.web_library.domain.product.*;
import com.danven.web_library.dto.CategoryDto;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.dto.ProductDto;
import com.danven.web_library.dto.ProductEditDto;
import com.danven.web_library.exceptions.ValidationException;
import com.danven.web_library.repository.CategoryRepository;
import com.danven.web_library.repository.ImageRepository;
import com.danven.web_library.repository.ProductRepository;
import com.danven.web_library.util.ImageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository  productRepo;
    private final CategoryRepository categoryRepo;
    private final ImageRepository    imageRepo;

    public ProductServiceImpl(ProductRepository productRepo,
                              CategoryRepository categoryRepo,
                              ImageRepository imageRepo) {
        this.productRepo  = productRepo;
        this.categoryRepo = categoryRepo;
        this.imageRepo    = imageRepo;
    }

    /**
     * Returns all products as DTOs, eagerly loading categories and images.
     *
     * @return list of {@link ProductDto}
     */

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepo.findAllWithCategoriesAndImages().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    /**
     * Returns all products in the given category, or all products if categoryId is 0.
     *
     * @param categoryId the category filter (0 = all)
     * @return list of filtered {@link ProductDto}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        if (categoryId == 0) {
            return getAllProducts();
        }
        return productRepo.findAllWithCategoriesAndImages().stream()
                .filter(p -> p.getCategories().stream()
                        .anyMatch(c -> c.getId().equals(categoryId)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve products by category name (case-insensitive).
     *
     * @param categoryName the name of the category
     * @return a list of filtered {@link ProductDto}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategoryName(String categoryName) {
        return productRepo.findAllWithCategoriesAndImages().stream()
                .filter(p -> p.getCategories().stream()
                        .anyMatch(c -> c.getName().equalsIgnoreCase(categoryName)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }


/**
 * Retrieve a single product by its ID, or throw if not found.
 *
 * @param productId the ID of the product
 * @return the corresponding {@link ProductDto}
 * @throws ValidationException if no product exists with the given ID
 */
    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId) {
        Product p = productRepo.findByIdWithCategoriesAndImages(productId)
                .orElseThrow(() -> new ValidationException("Product not found: " + productId));
        return toDto(p);
    }


    /**
     * Create a new product (with its specific subtype) from the given edit DTO,
     * persist it along with any uploaded images, and return the saved DTO.
     *
     * @param dto the {@link ProductEditDto} carrying creation data
     * @return the saved {@link ProductDto} including generated IDs
     * @throws IOException          when reading image files fails
     * @throws ValidationException  when category validation fails or required data is missing
     */
    @Override
    @Transactional
    public ProductDto addProduct(ProductEditDto dto) throws IOException {
        // 1) Валидируем и загружаем категории
        Set<Category> categories = fetchAndValidateCategories(dto.getCategoryIds());

        // 2) Инстанциируем entity из DTO
        Product product = instantiate(dto, categories);
        product.setCategories(categories);

        // 3) Сохраняем продукт, чтобы получить сгенерированный ID
        Product saved = productRepo.save(product);

        // 4) Сохраняем новые изображения и устанавливаем preview по ключу
        List<MultipartFile> files = dto.getNewImages();
        if (files != null && !files.isEmpty()) {
            String key = dto.getPreviewImageKey();
            int previewIndex = -1;

            // Извлекаем индекс изображения из ключа вида "new-0", "new-1"
            if (key != null && key.startsWith("new-")) {
                try {
                    previewIndex = Integer.parseInt(key.substring(4));
                } catch (NumberFormatException ignored) {}
            }

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file != null && !file.isEmpty()) {
                    ImageFormat format = ImageFormat.fromContentType(file.getContentType());
                    Image image = new Image();
                    image.setFormat(format);
                    image.setImage(file.getBytes());
                    image.setPreview(i == previewIndex); // только выбранное будет preview
                    image.setProduct(saved);
                    saved.addImage(image);
                    imageRepo.save(image);
                }
            }

            // Если preview не был установлен — ставим его на первое изображение
            boolean hasPreview = saved.getImages().stream().anyMatch(Image::isPreview);
            if (!hasPreview && !saved.getImages().isEmpty()) {
                saved.getImages().iterator().next().setPreview(true);
            }
        }


        // 5) Теперь у saved.getImages() есть все Image (в том числе новые),
        //    и можно сразу получить из них base64 через ваш util внутри toDto.
        //    Конвертим saved в DTO — он уже содержит id, списки картинок и категорий.
        ProductDto result = toDto(saved);

        return result;
    }


    /**
     * Update an existing product with data from the edit DTO:
     * basic fields, subtype-specific fields, categories, and images (add/remove/preview).
     *
     * @param productId the ID of the product to update
     * @param dto       the {@link ProductEditDto} carrying update data
     * @throws IOException         when reading image files fails
     * @throws ValidationException when the product is not found or validation fails
     */
    @Override
    @Transactional
    public void editProduct(Long productId, ProductEditDto dto) throws IOException {
        Product product = productRepo.findByIdWithCategoriesAndImages(productId)
                .orElseThrow(() -> new ValidationException("Product not found: " + productId));

        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setPrice(Optional.ofNullable(dto.getPrice()).orElse(BigDecimal.ZERO));
        product.setCalorie(dto.getCalorie());
        product.setWeightInGrams(dto.getWeightInGrams());

        applySubclassUpdates(product, dto);

        var cats = fetchAndValidateCategories(dto.getCategoryIds());
        product.setCategories(cats);

        Set<Long> removeIds = new HashSet<>();
        if (dto.getRemoveImageIds() != null) {
            removeIds.addAll(dto.getRemoveImageIds());
        }

        product.getImages().stream()
                .filter(img -> img.getId() != null && !removeIds.contains(img.getId()))
                .forEach(img -> img.setPreview(false));

        List<Image> newImages = new ArrayList<>();
        List<MultipartFile> files = dto.getNewImages();
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file != null && !file.isEmpty()) {
                    Image img = new Image();
                    img.setFormat(ImageFormat.valueOf(
                            file.getContentType().split("/")[1].toUpperCase()));
                    img.setImage(file.getBytes());
                    img.setPreview(false); // пока не preview
                    product.addImage(img);
                    newImages.add(img);
                }
            }
        }

        // D) Ставим preview по ключу
        String key = dto.getPreviewImageKey();
        if (key != null) {
            if (key.startsWith("existing-")) {
                try {
                    Long previewId = Long.parseLong(key.substring(9).replaceAll("[^\\d]", ""));
                    product.getImages().stream()
                            .filter(img -> Objects.equals(img.getId(), previewId) && !removeIds.contains(previewId))
                            .findFirst()
                            .ifPresent(img -> img.setPreview(true));
                } catch (NumberFormatException ex) {
                    throw new ValidationException("Некорректный preview ID: " + key);
                }
            } else if (key.startsWith("new-")) {
                try {
                    int index = Integer.parseInt(key.substring(4).replaceAll("[^\\d]", ""));
                    if (index >= 0 && index < newImages.size()) {
                        newImages.get(index).setPreview(true);
                    }
                } catch (NumberFormatException ex) {
                    throw new ValidationException("Некорректный индекс нового изображения: " + key);
                }
            }
        }

        // E) Удаляем изображения — в самую последнюю очередь
        if (!removeIds.isEmpty()) {
            List<Image> toRemove = product.getImages().stream()
                    .filter(img -> img.getId() != null && removeIds.contains(img.getId()))
                    .collect(Collectors.toList());

            for (Image img : toRemove) {
                product.removeImage(img);
                imageRepo.delete(img);
            }
        }

        // F) Если preview не установлен — назначаем первое изображение
        if (product.getImages().stream().noneMatch(Image::isPreview) && !product.getImages().isEmpty()) {
            product.getImages().iterator().next().setPreview(true);
        }

        // G) Сохраняем
        productRepo.save(product);
    }



    /**
     * Delete a product by its ID, including all associated images.
     *
     * @param productId the ID of the product to delete
     * @throws ValidationException if the product does not exist
     */
    @Override
    @Transactional
    public void removeProduct(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ValidationException("Продукт с id=" + productId + " не найден"));

        product.getImages().forEach(imageRepo::delete); // удалить изображения
        productRepo.delete(product);
    }


    /**
     * Retrieve the raw {@link Product} entity by its ID.
     * Useful for internal operations when the entity is needed.
     *
     * @param productId the ID of the product
     * @return the {@link Product} entity
     * @throws NoSuchElementException if no product is found
     */
    @Override
    @Transactional(readOnly = true)
    public Product getProductEntityById(Long productId) {
        return productRepo.findByIdWithCategoriesAndImages(productId)
                .orElseThrow(() ->
                        new NoSuchElementException("Продукт с id=" + productId + " не найден")
                );
    }

    /**
     * Delete a single image from a product without deleting the product itself.
     *
     * @param prodId the ID of the product
     * @param imgId  the ID of the image to delete
     * @throws ResponseStatusException if the product or image is not found
     */
    @Override
    @Transactional
    public void deleteImage(Long prodId, Long imgId) {
        Product product = productRepo.findById(prodId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found: " + prodId
                        )
                );

        boolean removed = product.getImages().removeIf(img -> img.getId().equals(imgId));
        if (!removed) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Image not found: " + imgId
            );
        }
        // flush happens on transaction commit
    }



    private Set<Category> fetchAndValidateCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            throw new ValidationException("At least one category must be selected");
        var found = categoryRepo.findAllById(ids);
        if (found.size() != new HashSet<>(ids).size())
            throw new ValidationException("Some categories not found");
        return new HashSet<>(found);
    }

    private Product instantiate(ProductEditDto dto, Set<Category> cats) {
        return switch (dto.getType()) {
            case "MILK_COCKTAIL" -> new MilkCocktail(
                    dto.getProductName(), dto.getProductDescription(), dto.getPrice(),
                    dto.getCalorie(), dto.getWeightInGrams(),
                    Optional.ofNullable(dto.getAlcohol()).orElse(0.0),
                    Optional.ofNullable(dto.getCarbonated()).orElse(false),
                    dto.getIceCreamType(),
                    Optional.ofNullable(dto.getSugarPerGram()).orElse(0.0),
                    cats);
            case "DESSERT" -> new Dessert(
                    dto.getProductName(), dto.getProductDescription(), dto.getPrice(),
                    dto.getCalorie(), dto.getWeightInGrams(),
                    Optional.ofNullable(dto.getSugarPerGram()).orElse(0.0),
                    cats);
            case "FOOD" -> new Food(
                    dto.getProductName(), dto.getProductDescription(), dto.getPrice(),
                    dto.getCalorie(), dto.getWeightInGrams(), cats,
                    Optional.ofNullable(dto.getIngredientsRaw())
                            .map(raw -> Stream.of(raw.split(","))
                                    .map(String::trim).filter(s->!s.isEmpty())
                                    .collect(Collectors.toList()))
                            .orElse(Collections.emptyList()));
            default -> new Drink(
                    dto.getProductName(), dto.getProductDescription(), dto.getPrice(),
                    dto.getCalorie(), dto.getWeightInGrams(),
                    Optional.ofNullable(dto.getAlcohol()).orElse(0.0),
                    Optional.ofNullable(dto.getCarbonated()).orElse(false),
                    cats);
        };
    }

    private void applySubclassUpdates(Product product, ProductEditDto dto) {
        if (product instanceof MilkCocktail mc) {
            Optional.ofNullable(dto.getAlcohol())
                    .ifPresent(mc::setAlcohol);
            Optional.ofNullable(dto.getCarbonated())
                    .ifPresent(mc::setCarbonated);
            Optional.ofNullable(dto.getIceCreamType())
                    .ifPresent(mc::setIceCreamType);
            Optional.ofNullable(dto.getSugarPerGram())
                    .ifPresent(mc::setSugarAmountGramProduct);

        } else if (product instanceof Dessert d) {
            Optional.ofNullable(dto.getSugarPerGram())
                    .ifPresent(d::setSugarAmountGramProduct);

        } else if (product instanceof Food f) {
            Optional.ofNullable(dto.getIngredientsRaw()).ifPresent(raw -> {
                var list = Stream.of(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                f.setIngredients(list);
            });

        } else if (product instanceof Drink dr) {
            Optional.ofNullable(dto.getAlcohol())
                    .ifPresent(dr::setAlcohol);
            Optional.ofNullable(dto.getCarbonated())
                    .ifPresent(dr::setCarbonated);
        }
    }


    private ProductDto toDto(Product p) {
        Double sugar = (p instanceof Dessert ds)      ? ds.getSugarAmountGramProduct()
                : (p instanceof MilkCocktail mc) ? mc.getSugarAmountGramProduct()
                : null;
        IceCreamType ice = (p instanceof MilkCocktail mc) ? mc.getIceCreamType() : null;

        List<ImageDto> imgs = p.getImages().stream()
                .map(img -> new ImageDto(
                        img.getId(), img.isPreview(), img.getFormat().name(),
                        ImageUtil.getImgData(img.getImage())
                ))
                .collect(Collectors.toList());

        return new ProductDto(
                p.getId(), p.getProductName(), p.getProductDescription(),
                p.getPrice(), p.getCalorie(), p.getWeightInGrams(),
                p.getCategories().stream()
                        .map(c -> new CategoryDto(c.getId(), c.getName()))
                        .collect(Collectors.toList()),
                imgs,
                (p instanceof MilkCocktail) ? "MILK_COCKTAIL"
                        : (p instanceof Dessert)     ? "DESSERT"
                        : (p instanceof Food)        ? "FOOD"
                        :                              "DRINK",
                (p instanceof Drink d) ? d.getAlcohol() : null,
                (p instanceof Drink d) ? d.isCarbonatedDrink() : null,
                sugar,
                ice,
                (p instanceof Food f)  ? f.getIngredients() : null
        );

    }
}
