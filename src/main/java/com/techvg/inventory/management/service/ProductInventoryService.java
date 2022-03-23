package com.techvg.inventory.management.service;

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
    public List<ProductDTO> countProductInventoriesStock(ProductInventoryCriteria criteria, Pageable page) {
        List<ProductDTO> productsList = new ArrayList<ProductDTO>();

        //---------------- Calculate Stock when ProductId not given in Criteria---------------
        if (criteria.getProductId() == null && criteria.getWareHouseId() != null) {
            ProductCriteria prodCriteria = new ProductCriteria();
            ProductTypeFilter productFilter = new ProductTypeFilter();
            productFilter.setEquals(ProductType.RAWMATERIAL);
            prodCriteria.setProductType(productFilter);
            List<ProductDTO> productList = productQueryService.findByCriteria(prodCriteria);

            for (ProductDTO productObj : productList) {
                LongFilter idFilter = new LongFilter();
                idFilter.setEquals(productObj.getId());
                criteria.setProductId(idFilter);

                List<ProductInventoryDTO> stockList = productInventoryQueryService.findByCriteria(criteria);
                int productStock = this.countInventoryStock(stockList);
                productObj.setTotalStock(productStock);
                productObj.setWareHouseId(criteria.getWareHouseId().getEquals());
                return productList;
            }
        } //---------------- Calculate Stock when WareHouseId not given in Criteria---------------
        else if (criteria.getWareHouseId() == null && criteria.getProductId() != null) {
            WareHouseCriteria wareHouseCri = new WareHouseCriteria();
            List<WareHouseDTO> wareHouseList = wareHouseQueryService.findByCriteria(wareHouseCri);

            for (WareHouseDTO wareaHouseObj : wareHouseList) {
                LongFilter idFilter = new LongFilter();
                idFilter.setEquals(wareaHouseObj.getId());
                criteria.setWareHouseId(idFilter);
                List<ProductInventoryDTO> stockLists = productInventoryQueryService.findByCriteria(criteria);
                if (!stockLists.isEmpty()) {
                    int productStock = this.countInventoryStock(stockLists);
                    ProductDTO product = stockLists.iterator().next().getProduct();
                    product.setTotalStock(productStock);
                    product.setWareHouseId(wareaHouseObj.getId());
                    productsList.add(product);
                }
            }
            return productsList;
        } else { //both product Id and warehouse id available
            List<ProductInventoryDTO> stockLists = productInventoryQueryService.findByCriteria(criteria);
            int productStock = this.countInventoryStock(stockLists);
            ProductDTO product = stockLists.iterator().next().getProduct();
            product.setWareHouseId(criteria.getWareHouseId().getEquals());
            product.setTotalStock(productStock);
            productsList.add(product);
        }
        return productsList;
    }
}
