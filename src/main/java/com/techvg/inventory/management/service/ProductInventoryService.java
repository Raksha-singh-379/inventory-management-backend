package com.techvg.inventory.management.service;

import com.techvg.inventory.management.domain.Product;
import com.techvg.inventory.management.domain.ProductInventory;
import com.techvg.inventory.management.domain.enumeration.ProductType;
import com.techvg.inventory.management.repository.ProductInventoryRepository;
import com.techvg.inventory.management.service.criteria.ProductCriteria;
import com.techvg.inventory.management.service.criteria.ProductCriteria.ProductTypeFilter;
import com.techvg.inventory.management.service.criteria.ProductInventoryCriteria;
import com.techvg.inventory.management.service.criteria.WareHouseCriteria;
import com.techvg.inventory.management.service.dto.ProductDTO;
import com.techvg.inventory.management.service.dto.ProductInventoryDTO;
import com.techvg.inventory.management.service.dto.WareHouseDTO;
import com.techvg.inventory.management.service.mapper.ProductInventoryMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.filter.LongFilter;

/**
 * Service Implementation for managing {@link ProductInventory}.
 */
@Service
@Transactional
public class ProductInventoryService {

    private final Logger log = LoggerFactory.getLogger(ProductInventoryService.class);

    private final ProductInventoryRepository productInventoryRepository;

    private final ProductInventoryMapper productInventoryMapper;

    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private ProductInventoryQueryService productInventoryQueryService;

    @Autowired
    private WareHouseQueryService wareHouseQueryService;

    public ProductInventoryService(ProductInventoryRepository productInventoryRepository, ProductInventoryMapper productInventoryMapper) {
        this.productInventoryRepository = productInventoryRepository;
        this.productInventoryMapper = productInventoryMapper;
    }

    /**
     * Save a productInventory.
     *
     * @param productInventoryDTO the entity to save.
     * @return the persisted entity.
     */
    public ProductInventoryDTO save(ProductInventoryDTO productInventoryDTO) {
        log.debug("Request to save ProductInventory : {}", productInventoryDTO);
        ProductInventory productInventory = productInventoryMapper.toEntity(productInventoryDTO);
        productInventory = productInventoryRepository.save(productInventory);
        return productInventoryMapper.toDto(productInventory);
    }

    /**
     * Partially update a productInventory.
     *
     * @param productInventoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProductInventoryDTO> partialUpdate(ProductInventoryDTO productInventoryDTO) {
        log.debug("Request to partially update ProductInventory : {}", productInventoryDTO);

        return productInventoryRepository
            .findById(productInventoryDTO.getId())
            .map(existingProductInventory -> {
                productInventoryMapper.partialUpdate(existingProductInventory, productInventoryDTO);

                return existingProductInventory;
            })
            .map(productInventoryRepository::save)
            .map(productInventoryMapper::toDto);
    }

    /**
     * Get all the productInventories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProductInventoryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ProductInventories");
        return productInventoryRepository.findAll(pageable).map(productInventoryMapper::toDto);
    }

    /**
     * Get all the productInventories with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ProductInventoryDTO> findAllWithEagerRelationships(Pageable pageable) {
        return productInventoryRepository.findAllWithEagerRelationships(pageable).map(productInventoryMapper::toDto);
    }

    /**
     * Get one productInventory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProductInventoryDTO> findOne(Long id) {
        log.debug("Request to get ProductInventory : {}", id);
        return productInventoryRepository.findOneWithEagerRelationships(id).map(productInventoryMapper::toDto);
    }

    /**
     * Delete the productInventory by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ProductInventory : {}", id);
        productInventoryRepository.deleteById(id);
    }

    /**
     * Get productInventories Stock Count.
     *
     * @param ProductInventoryDTO List to calculate totalQuantityAvailable .
     * @return int totalQuantityAvailable count.
     */
    public int countInventoryStock(List<ProductInventoryDTO> stockList) {
        int sumInwardQty = 0;
        int sumOutwardQty = 0;
        for (ProductInventoryDTO stockObj : stockList) {
            if (stockObj.getInwardQty() != null) {
                Long stockcount = Long.valueOf(stockObj.getInwardQty()).longValue();

                sumInwardQty += stockcount;
            } else if (stockObj.getOutwardQty() != null) {
                Long stockcount = Long.valueOf(stockObj.getOutwardQty()).longValue();

                sumOutwardQty += stockcount;
            }
        }
        int totalQuantityAvailable = sumInwardQty - sumOutwardQty;
        return totalQuantityAvailable;
    }

    /**
     * Get all the productInventories Stock Count.
     *
     * @param ProductInventoryCriteria criteria to calculate.
     * @return the list of product entities.
     */
    public Page<ProductDTO> countProductInventoriesStock(ProductInventoryCriteria criteria, ProductCriteria pdCriteria, Pageable page) {
        List<ProductDTO> productsList = new ArrayList<ProductDTO>();

        // --------------------------If product search criteria is given

        if (pdCriteria.getCasNumber() != null || pdCriteria.getCatlogNumber() != null || pdCriteria.getProductName() != null) {
            Page<ProductDTO> productList = productQueryService.findByCriteria(pdCriteria, page);

            if (criteria.getWareHouseId() != null) {
                for (ProductDTO productObj : productList.getContent()) {
                    LongFilter idFilter = new LongFilter();
                    idFilter.setEquals(productObj.getId());
                    criteria.setProductId(idFilter);

                    Page<ProductInventoryDTO> stockList = productInventoryQueryService.findByCriteria(criteria, page);
                    int productStock = this.countInventoryStock(stockList.getContent());
                    productObj.setTotalStock(productStock);
                    productObj.setWareHouseId(criteria.getWareHouseId().getEquals());
                }
                return productList;
            } else if (criteria.getWareHouseId() == null) {
                WareHouseCriteria wareHouseCri = new WareHouseCriteria();
                Page<WareHouseDTO> wareHouseList = wareHouseQueryService.findByCriteria(wareHouseCri, page);

                for (WareHouseDTO wareaHouseObj : wareHouseList.getContent()) {
                    LongFilter idFilter = new LongFilter();
                    idFilter.setEquals(wareaHouseObj.getId());
                    criteria.setWareHouseId(idFilter);
                    Page<ProductDTO> productLists = productQueryService.findByCriteria(pdCriteria, page);
                    for (ProductDTO productObj : productLists) {
                        idFilter.setEquals(productObj.getId());
                        criteria.setProductId(idFilter);
                        Page<ProductInventoryDTO> stockLists = productInventoryQueryService.findByCriteria(criteria, page);
                        if (!stockLists.getContent().isEmpty()) {
                            int productStock = this.countInventoryStock(stockLists.getContent());
                            productObj.setTotalStock(productStock);
                            productObj.setWareHouseId(wareaHouseObj.getId());
                        }
                    }

                    return productLists;
                }
            } else if (criteria != null) {
                // ---------------- Calculate Stock when ProductId not given in
                // Criteria---------------

                if (criteria.getProductId() == null && criteria.getWareHouseId() != null) {
                    ProductCriteria prodCriteria = new ProductCriteria();
                    ProductTypeFilter productFilter = new ProductTypeFilter();
                    productFilter.setEquals(ProductType.RAWMATERIAL);
                    prodCriteria.setProductType(productFilter);
                    Page<ProductDTO> productLists = productQueryService.findByCriteria(prodCriteria, page);

                    for (ProductDTO productObj : productLists.getContent()) {
                        LongFilter idFilter = new LongFilter();
                        idFilter.setEquals(productObj.getId());
                        criteria.setProductId(idFilter);

                        Page<ProductInventoryDTO> stockList = productInventoryQueryService.findByCriteria(criteria, page);
                        int productStock = this.countInventoryStock(stockList.getContent());
                        productObj.setTotalStock(productStock);
                        productObj.setWareHouseId(criteria.getWareHouseId().getEquals());
                    }
                    return productLists;
                } // both product Id and warehouse id available
                else if (criteria.getWareHouseId() != null && criteria.getProductId() != null) {
                    Page<ProductInventoryDTO> stockLists = productInventoryQueryService.findByCriteria(criteria, page);
                    int productStock = this.countInventoryStock(stockLists.getContent());
                    ProductDTO product = stockLists.iterator().next().getProduct();
                    product.setWareHouseId(criteria.getWareHouseId().getEquals());
                    product.setTotalStock(productStock);
                    productsList.add(product);
                    return this.convertListIntoPagable(productsList, page);
                }
                //---------------------Calculate Stock when productId is given in Criteria---------------

                if (criteria.getWareHouseId() == null && criteria.getProductId() != null) {
                    WareHouseCriteria wareHouseCri = new WareHouseCriteria();
                    Page<WareHouseDTO> wareHouseList = wareHouseQueryService.findByCriteria(wareHouseCri, page);
                    for (WareHouseDTO wareaHouseObj : wareHouseList.getContent()) {
                        LongFilter idFilter = new LongFilter();
                        idFilter.setEquals(wareaHouseObj.getId());
                        criteria.setWareHouseId(idFilter);

                        Page<ProductInventoryDTO> stockLists = productInventoryQueryService.findByCriteria(criteria, page);
                        if (!stockLists.getContent().isEmpty()) {
                            int productStock = this.countInventoryStock(stockLists.getContent());
                            ProductDTO product = stockLists.getContent().iterator().next().getProduct();
                            product.setTotalStock(productStock);
                            product.setWareHouseId(wareaHouseObj.getId());
                            productsList.add(product);
                        }

                        return this.convertListIntoPagable(productsList, page);
                    }
                }
                // ---------------- Calculate Stock when WareHouseId and productId not given in
                // Criteria---------------
                else {
                    WareHouseCriteria wareHouseCri = new WareHouseCriteria();
                    Page<WareHouseDTO> wareHouseList = wareHouseQueryService.findByCriteria(wareHouseCri, page);
                    Page<ProductDTO> productLists = productQueryService.findByCriteria(pdCriteria, page);
                    for (ProductDTO productObj : productLists) {
                        LongFilter idFilter = new LongFilter();
                        idFilter.setEquals(productObj.getId());
                        criteria.setProductId(idFilter);

                        for (WareHouseDTO wareaHouseObj : wareHouseList.getContent()) {
                            idFilter.setEquals(wareaHouseObj.getId());
                            criteria.setWareHouseId(idFilter);

                            Page<ProductInventoryDTO> stockLists = productInventoryQueryService.findByCriteria(criteria, page);

                            if (!stockLists.getContent().isEmpty()) {
                                int productStock = this.countInventoryStock(stockLists.getContent());
                                productObj.setTotalStock(productStock);
                                productObj.setWareHouseId(wareaHouseObj.getId());
                            }
                        }
                    }
                    return productLists;
                }
            }
        }
        return this.convertListIntoPagable(productsList, page);
    }

    public Page<ProductDTO> convertListIntoPagable(List<ProductDTO> productsList, Pageable page) {
        int startOfPage = page.getPageNumber() * page.getPageSize();
        if (startOfPage > productsList.size()) {
            return new PageImpl<>(new ArrayList<>(), page, 0);
        }

        int endOfPage = Math.min(startOfPage + page.getPageSize(), productsList.size());
        return new PageImpl<>(productsList.subList(startOfPage, endOfPage), page, productsList.size());
    }
}
