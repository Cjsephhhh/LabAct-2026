package edu.cit.alvarado.supplier.adapter;

import edu.cit.alvarado.supplier.SupplierOrderResult;
import edu.cit.alvarado.supplier.SupplierOrderStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.springframework.stereotype.Component;

@Component
class LegacySupplyTranslator {

    SupplierOrderResult translatePurchaseOrder(
            String xml,
            String productId
    ) {

        Document document = parse(xml);

        String poNumber = text(document, "PoNumber");
        String buyerRef = text(document, "BuyerRef");
        int quantity = integer(document, "Qty");
        String uom = text(document, "Uom");
        int statusCode = integer(document, "StatusCode");

        SupplierOrderStatus status = mapStatus(statusCode);

        int packSize = packSizeFor(productId);

        int units = quantity * packSize;

        return new SupplierOrderResult(
                buyerRef,
                poNumber,
                productId,
                quantity,
                units,
                status
        );
    }

    SupplierOrderResult translatePurchaseOrderList(
            String xml,
            String productId
    ) {

        Document document = parse(xml);

        NodeList orders = document.getElementsByTagName("PurchaseOrder");

        if (orders.getLength() == 0) {
            return null;
        }

        var order = orders.item(0);

        String poNumber = childText(order, "PoNumber");
        String buyerRef = childText(order, "BuyerRef");
        int quantity = Integer.parseInt(childText(order, "Qty"));
        int statusCode = Integer.parseInt(childText(order, "StatusCode"));

        SupplierOrderStatus status = mapStatus(statusCode);

        int packSize = packSizeFor(productId);
        int units = quantity * packSize;

        return new SupplierOrderResult(
                buyerRef,
                poNumber,
                productId,
                quantity,
                units,
                status
        );
    }

    String sessionToken(String xml) {
        Document document = parse(xml);
        return text(document, "SessionToken");
    }

    private SupplierOrderStatus mapStatus(int statusCode) {

        return switch (statusCode) {
            case 10 -> SupplierOrderStatus.ACCEPTED;
            case 20 -> SupplierOrderStatus.PICKING;
            case 30 -> SupplierOrderStatus.SHIPPED;
            case 40 -> SupplierOrderStatus.DELIVERED;
            default -> SupplierOrderStatus.UNKNOWN;
        };
    }

    private int packSizeFor(String productId) {

        return switch (productId) {
            case "P100" -> 24;
            case "P200" -> 20;
            case "P300" -> 12;
            default -> throw new IllegalArgumentException(
                    "No LegacySupply mapping configured for product: " + productId
            );
        };
    }

    private Document parse(String xml) {

        try {
            var factory = DocumentBuilderFactory.newInstance();

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );

            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
            );

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            var builder = factory.newDocumentBuilder();

            return builder.parse(
                    new InputSource(new StringReader(xml))
            );

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unable to parse LegacySupply XML response",
                    e
            );
        }
    }

    private String text(
            Document document,
            String element
    ) {

        NodeList nodes = document.getElementsByTagName(element);

        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException(
                    "Missing XML element: " + element
            );
        }

        return nodes.item(0)
                .getTextContent()
                .trim();
    }

    private int integer(
            Document document,
            String element
    ) {

        return Integer.parseInt(text(document, element));
    }

    private String childText(
            org.w3c.dom.Node parent,
            String element
    ) {

        NodeList nodes = ((org.w3c.dom.Element) parent)
                .getElementsByTagName(element);

        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException(
                    "Missing XML element: " + element
            );
        }

        return nodes.item(0)
                .getTextContent()
                .trim();
    }
}