package com.moon.vault.common;

import java.util.List;

public record PageResp<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
}
