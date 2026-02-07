package io.github.witsisland.inspirehub.di

import io.github.witsisland.inspirehub.data.storage.NSUserDefaultsTokenStorage
import io.github.witsisland.inspirehub.data.storage.TokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TokenStorage> { NSUserDefaultsTokenStorage() }
}
