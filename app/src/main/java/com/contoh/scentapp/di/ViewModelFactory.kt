package com.contoh.scentapp.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.contoh.scentapp.data.repository.CartRepository
import com.contoh.scentapp.ui.auth.AuthViewModel
import com.contoh.scentapp.ui.cart.CartViewModel
import com.contoh.scentapp.ui.detail.DetailViewModel
import com.contoh.scentapp.ui.favorite.FavoriteViewModel
import com.contoh.scentapp.ui.home.HomeViewModel
import com.contoh.scentapp.ui.profile.ProfileViewModel
import com.contoh.scentapp.ui.sales.SalesViewModel
import com.contoh.scentapp.ui.search.SearchViewModel

object ViewModelFactory {

    private val cartRepo: CartRepository
        get() = CartRepository.getInstance()

    fun authFactory(application: Application) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val authRepo = com.contoh.scentapp.data.repository.AuthRepositoryImpl.getInstance()
            val loginUseCase = com.contoh.scentapp.domain.usecase.auth.LoginUseCase(authRepo)
            val registerUseCase = com.contoh.scentapp.domain.usecase.auth.RegisterUseCase(authRepo)
            val logoutUseCase = com.contoh.scentapp.domain.usecase.auth.LogoutUseCase(authRepo)
            return AuthViewModel(application, loginUseCase, registerUseCase, logoutUseCase) as T
        }
    }

    // HomeViewModel menggunakan Use Cases
    fun homeFactory() = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = com.contoh.scentapp.data.repository.ProductRepositoryImpl()
            val favoriteRepo = com.contoh.scentapp.data.repository.FavoriteRepository()
            val getHomeProductsUseCase = com.contoh.scentapp.domain.usecase.GetHomeProductsUseCase(productRepo, favoriteRepo)
            val toggleFavoriteUseCase = com.contoh.scentapp.domain.usecase.ToggleFavoriteUseCase(favoriteRepo)
            return HomeViewModel(getHomeProductsUseCase, toggleFavoriteUseCase) as T
        }
    }

    fun searchFactory(application: Application) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = com.contoh.scentapp.data.repository.ProductRepositoryImpl()
            val favoriteRepo = com.contoh.scentapp.data.repository.FavoriteRepository()
            val searchHistoryRepo = (application as com.contoh.scentapp.AppApplication).searchHistoryRepository
            
            val getAllProductsUseCase = com.contoh.scentapp.domain.usecase.product.GetAllProductsUseCase(productRepo, favoriteRepo)
            val toggleFavoriteUseCase = com.contoh.scentapp.domain.usecase.ToggleFavoriteUseCase(favoriteRepo)
            
            val getRecentSearchesUseCase = com.contoh.scentapp.domain.usecase.search.GetRecentSearchesUseCase(searchHistoryRepo)
            val addSearchQueryUseCase = com.contoh.scentapp.domain.usecase.search.AddSearchQueryUseCase(searchHistoryRepo)
            val clearSearchHistoryUseCase = com.contoh.scentapp.domain.usecase.search.ClearSearchHistoryUseCase(searchHistoryRepo)
            
            return SearchViewModel(
                getAllProductsUseCase, 
                toggleFavoriteUseCase,
                getRecentSearchesUseCase,
                addSearchQueryUseCase,
                clearSearchHistoryUseCase
            ) as T
        }
    }

    fun detailFactory(firestoreId: String) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = com.contoh.scentapp.data.repository.ProductRepositoryImpl()
            val reviewRepo = com.contoh.scentapp.data.repository.ReviewRepository()
            val cartRepo = com.contoh.scentapp.data.repository.CartRepository.getInstance()
            
            val getProductDetailUseCase = com.contoh.scentapp.domain.usecase.product.GetProductDetailUseCase(productRepo)
            val getProductReviewsUseCase = com.contoh.scentapp.domain.usecase.product.GetProductReviewsUseCase(reviewRepo)
            val addToCartUseCase = com.contoh.scentapp.domain.usecase.cart.AddToCartUseCase(cartRepo)
            
            return DetailViewModel(firestoreId, getProductDetailUseCase, getProductReviewsUseCase, addToCartUseCase) as T
        }
    }

    // FavoriteViewModel menggunakan Use Cases
    fun favoriteFactory() = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = com.contoh.scentapp.data.repository.ProductRepositoryImpl()
            val favoriteRepo = com.contoh.scentapp.data.repository.FavoriteRepository()
            
            val getAllProductsUseCase = com.contoh.scentapp.domain.usecase.product.GetAllProductsUseCase(productRepo, favoriteRepo)
            val toggleFavoriteUseCase = com.contoh.scentapp.domain.usecase.ToggleFavoriteUseCase(favoriteRepo)
            
            return com.contoh.scentapp.ui.favorite.FavoriteViewModel(getAllProductsUseCase, toggleFavoriteUseCase) as T
        }
    }

    fun cartFactory() = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val cartRepo = com.contoh.scentapp.data.repository.CartRepository.getInstance()
            
            val getCartItemsUseCase = com.contoh.scentapp.domain.usecase.cart.GetCartItemsUseCase(cartRepo)
            val updateCartQuantityUseCase = com.contoh.scentapp.domain.usecase.cart.UpdateCartQuantityUseCase(cartRepo)
            val removeFromCartUseCase = com.contoh.scentapp.domain.usecase.cart.RemoveFromCartUseCase(cartRepo)
            val clearCartUseCase = com.contoh.scentapp.domain.usecase.cart.ClearCartUseCase(cartRepo)
            
            return CartViewModel(getCartItemsUseCase, updateCartQuantityUseCase, removeFromCartUseCase, clearCartUseCase) as T
        }
    }

    fun profileFactory(application: Application) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val authRepo = com.contoh.scentapp.data.repository.AuthRepositoryImpl.getInstance()
            val languageManager = com.contoh.scentapp.data.repository.LanguageManager.getInstance(application)
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            val getCurrentUserUseCase = com.contoh.scentapp.domain.usecase.auth.GetCurrentUserUseCase(authRepo)
            val updateProfileUseCase = com.contoh.scentapp.domain.usecase.profile.UpdateProfileUseCase(application, authRepo, firestore)
            val updatePasswordUseCase = com.contoh.scentapp.domain.usecase.auth.UpdatePasswordUseCase(authRepo)
            val getLanguageUseCase = com.contoh.scentapp.domain.usecase.profile.GetLanguageUseCase(languageManager)
            
            return ProfileViewModel(getCurrentUserUseCase, updateProfileUseCase, updatePasswordUseCase, getLanguageUseCase) as T
        }
    }

    fun salesFactory() = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = com.contoh.scentapp.data.repository.ProductRepositoryImpl()
            val getSellerProductsUseCase = com.contoh.scentapp.domain.usecase.product.GetSellerProductsUseCase(productRepo)
            val deleteProductUseCase = com.contoh.scentapp.domain.usecase.product.DeleteProductUseCase(productRepo)
            return SalesViewModel(getSellerProductsUseCase, deleteProductUseCase) as T
        }
    }

    fun addProductFactory(firestoreId: String? = null) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productRepo = com.contoh.scentapp.data.repository.ProductRepositoryImpl()
            val getProductDetailUseCase = com.contoh.scentapp.domain.usecase.product.GetProductDetailUseCase(productRepo)
            val addProductUseCase = com.contoh.scentapp.domain.usecase.product.AddProductUseCase(productRepo)
            val updateProductUseCase = com.contoh.scentapp.domain.usecase.product.UpdateProductUseCase(productRepo)
            val vm = com.contoh.scentapp.ui.sales.AddProductViewModel(getProductDetailUseCase, addProductUseCase, updateProductUseCase)
            if (!firestoreId.isNullOrBlank()) {
                vm.loadParfumForEdit(firestoreId)
            }
            return vm as T
        }
    }

    fun languageFactory(context: android.content.Context) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val languageManager = com.contoh.scentapp.data.repository.LanguageManager.getInstance(context)
            val getLanguageUseCase = com.contoh.scentapp.domain.usecase.profile.GetLanguageUseCase(languageManager)
            val setLanguageUseCase = com.contoh.scentapp.domain.usecase.profile.SetLanguageUseCase(languageManager)
            return com.contoh.scentapp.ui.profile.LanguageViewModel(getLanguageUseCase, setLanguageUseCase) as T
        }
    }
}