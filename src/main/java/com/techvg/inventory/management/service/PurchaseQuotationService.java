package com.techvg.inventory.management.service;

import com.techvg.inventory.management.domain.PurchaseQuotation;
import com.techvg.inventory.management.domain.PurchaseQuotationDetails;
import com.techvg.inventory.management.repository.PurchaseQuotationDetailsRepository;
import com.techvg.inventory.management.repository.PurchaseQuotationRepository;
import com.techvg.inventory.management.service.dto.PurchaseQuotationDTO;
import com.techvg.inventory.management.service.dto.PurchaseQuotationDetailsDTO;
import com.techvg.inventory.management.service.mapper.PurchaseQuotationDetailsMapper;
import com.techvg.inventory.management.service.mapper.PurchaseQuotationMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link PurchaseQuotation}.
 */
@Service
@Transactional
public class PurchaseQuotationService {

    private final Logger log = LoggerFactory.getLogger(PurchaseQuotationService.class);

    private final PurchaseQuotationRepository purchaseQuotationRepository;

    private final PurchaseQuotationMapper purchaseQuotationMapper;

    @Autowired
    private PurchaseQuotationDetailsRepository purchaseQuotationDetailsRepository;

    @Autowired
    private PurchaseQuotationDetailsMapper purchaseQuotationDetailsMapper;

    @Autowired
    private PurchaseQuotationDetailsService purchaseQuotationDetailsService;

    public PurchaseQuotationService(
        PurchaseQuotationRepository purchaseQuotationRepository,
        PurchaseQuotationMapper purchaseQuotationMapper
    ) {
        this.purchaseQuotationRepository = purchaseQuotationRepository;
        this.purchaseQuotationMapper = purchaseQuotationMapper;
    }

    /**
     * Save a purchaseQuotation.
     *
     * @param purchaseQuotationDTO the entity to save.
     * @return the persisted entity.
     */
    public PurchaseQuotationDTO save(PurchaseQuotationDTO purchaseQuotationDTO) {
        log.debug("Request to save PurchaseQuotation : {}", purchaseQuotationDTO);
        PurchaseQuotation purchaseQuotation = purchaseQuotationMapper.toEntity(purchaseQuotationDTO);
        purchaseQuotation = purchaseQuotationRepository.save(purchaseQuotation);

        // -------------------------------------------------------
        // -------------Create PurchaseQuotationDetails product wise
        if (purchaseQuotationDTO.getPurchaseQuotationDetails() != null && !purchaseQuotationDTO.getPurchaseQuotationDetails().isEmpty()) {
            List<PurchaseQuotationDetailsDTO> quotationDetailsList = purchaseQuotationDTO.getPurchaseQuotationDetails();
            for (int i = 0; i < quotationDetailsList.size(); i++) {
                PurchaseQuotationDetailsDTO detailsDto = quotationDetailsList.get(i);

                log.debug("Request to save PurchaseQuotationDetails : {}", detailsDto);

                if (detailsDto != null) {
                    PurchaseQuotationDTO pqDTO = new PurchaseQuotationDTO();
                    pqDTO.setId(purchaseQuotation.getId());
                    detailsDto.setPurchaseQuotation(pqDTO);
                    PurchaseQuotationDetails purchaseQuotationDetails = purchaseQuotationDetailsMapper.toEntity(detailsDto);
                    purchaseQuotationDetails = purchaseQuotationDetailsRepository.save(purchaseQuotationDetails);
                }
            }
        }

        purchaseQuotationDTO.setId(purchaseQuotation.getId());
        return purchaseQuotationDTO;
    }

    /**
     * Partially update a purchaseQuotation.
     *
     * @param purchaseQuotationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PurchaseQuotationDTO> partialUpdate(PurchaseQuotationDTO purchaseQuotationDTO) {
        log.debug("Request to partially update PurchaseQuotation : {}", purchaseQuotationDTO);

        return purchaseQuotationRepository
            .findById(purchaseQuotationDTO.getId())
            .map(existingPurchaseQuotation -> {
                purchaseQuotationMapper.partialUpdate(existingPurchaseQuotation, purchaseQuotationDTO);

                return existingPurchaseQuotation;
            })
            .map(purchaseQuotationRepository::save)
            .map(purchaseQuotationMapper::toDto);
    }

    /**
     * Get all the purchaseQuotations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PurchaseQuotationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PurchaseQuotations");
        return purchaseQuotationRepository.findAll(pageable).map(purchaseQuotationMapper::toDto);
    }

    /**
     * Get all the purchaseQuotations with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PurchaseQuotationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return purchaseQuotationRepository.findAllWithEagerRelationships(pageable).map(purchaseQuotationMapper::toDto);
    }

    /**
     * Get one purchaseQuotation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PurchaseQuotationDTO> findOne(Long id) {
        log.debug("Request to get PurchaseQuotation : {}", id);
        Optional<PurchaseQuotation> quotation = purchaseQuotationRepository.findOneWithEagerRelationships(id);
        quotation.get().setPurchaseQuotationDetails(null);
        return quotation.map(purchaseQuotationMapper::toDto);
    }

    /**
     * Delete the purchaseQuotation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete PurchaseQuotation : {}", id);
        purchaseQuotationRepository.deleteById(id);
    }
}
