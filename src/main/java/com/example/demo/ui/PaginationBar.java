package com.example.demo.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;

import java.util.function.IntConsumer;

final class PaginationBar extends HorizontalLayout {
    private final Button previous = new Button("Anterior", VaadinIcon.ANGLE_LEFT.create());
    private final Button next = new Button("Siguiente", VaadinIcon.ANGLE_RIGHT.create());
    private final Span status = new Span();
    private final Select<Integer> pageSize = new Select<>();
    private int currentPage;
    private int totalPages;

    PaginationBar(IntConsumer pageChange, IntConsumer pageSizeChange) {
        addClassName("pagination-bar");
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.END);
        previous.addClickListener(event -> pageChange.accept(currentPage - 1));
        next.addClickListener(event -> pageChange.accept(currentPage + 1));
        pageSize.setLabel("Filas por página");
        pageSize.setItems(5, 10, 20, 50);
        pageSize.setValue(10);
        pageSize.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) pageSizeChange.accept(event.getValue());
        });
        add(pageSize, previous, status, next);
    }

    void update(int page, int pages, long totalItems) {
        currentPage = page;
        totalPages = Math.max(pages, 1);
        status.setText("Página " + (currentPage + 1) + " de " + totalPages + " · " + totalItems + " registros");
        previous.setEnabled(currentPage > 0);
        next.setEnabled(currentPage + 1 < totalPages);
    }
}
