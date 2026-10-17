package com.qasioon.expensetracker.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.entity.CategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val app: ExpenseApp) : ViewModel() {

    val categories = app.categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, budget: Double?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            app.categoryRepository.insert(
                CategoryEntity(name = trimmed, monthlyBudget = budget)
            )
        }
    }

    fun update(id: Long, name: String, budget: Double?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val existing = app.categoryRepository.getById(id) ?: return@launch
            app.categoryRepository.update(
                existing.copy(name = trimmed, monthlyBudget = budget)
            )
        }
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch {
            app.categoryRepository.delete(category)
        }
    }

    companion object {
        fun factory(app: ExpenseApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { CategoriesViewModel(app) }
        }
    }
}
