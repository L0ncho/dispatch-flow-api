package com.dispatchflow.guides.domain.services;

import com.dispatchflow.guides.domain.entities.DispatchGuide;

import java.text.Normalizer;

public class GuidePdfPathBuilder {

    public String buildRelativePath(DispatchGuide guide) {
        String date = guide.getDispatchDate().toString();
        String carrierSlug = slugify(guide.getCarrierName());
        String fileName = "guide-" + guide.getId().value() + ".pdf";
        return "guides/" + date + "/" + carrierSlug + "/" + fileName;
    }

    private String slugify(String carrierName) {
        String normalized = Normalizer.normalize(carrierName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String lowercased = normalized.toLowerCase();
        String withHyphens = lowercased.replaceAll("[^a-z0-9]+", "-");
        return withHyphens.replaceAll("^-+|-+$", "");
    }
}
