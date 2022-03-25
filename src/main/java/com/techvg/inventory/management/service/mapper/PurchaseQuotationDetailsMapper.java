package com.techvg.inventory.management.service.mapper;

import com.techvg.inventory.management.domain.PurchaseQuotationDetails;
import com.techvg.inventory.management.service.dto.PurchaseQuotationDetailsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PurchaseQuotationDetails} and its DTO {@link PurchaseQuotationDetailsDTO}.
 */
@Mapper(componentModel = "spring", uses = { ProductMapper.class, PurchaseQuotationMapper.class })
public interface PurchaseQuotationDetailsMapper extends EntityMapper<PurchaseQuotationDetailsDTO, PurchaseQuotationDetails> {
    @Mapping(target = "product.id", source = "product.id")
    @Mapping(target = "purchaseQuotation.id", source = "purchaseQuotation.id")
    PurchaseQuotationDetailsDTO toDto(PurchaseQuotationDetails s);
}
