package com.amit.crud.service;


import com.amit.crud.entity.PromoCode;
import com.amit.crud.repository.PromoCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PromoService {
    private final PromoCodeRepository promoCodeRepository;

    public PromoService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    public Optional<PromoCode> findByCode(String code){
        return promoCodeRepository.findByCode(code);
    }

    public boolean isValid(PromoCode p){
        if (p==null) return false;
        if (!p.isActive()) return false;
        if (p.getExpiryDate()!=null && p.getExpiryDate().isBefore(LocalDate.now())) return false;
        return true;
    }
}

