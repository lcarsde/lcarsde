package de.atennert.lcarsde.files

import de.atennert.rx.Observable
import de.atennert.rx.Subscription
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.opendir
import platform.posix.readdir

@OptIn(ExperimentalForeignApi::class)
private class DirectoryIterator(val dir: CPointer<cnames.structs.__dirstream>?) : Iterable<CPointer<dirent>> {
    override fun iterator(): Iterator<CPointer<dirent>> {
        return object : Iterator<CPointer<dirent>> {
            var file: CPointer<dirent>? = null

            override fun hasNext(): Boolean {
                file = readdir(dir)
                return file != null
            }

            override fun next(): CPointer<dirent> {
                return file!!
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun list(path: String): Observable<String> {
    return Observable { subscriber ->
        val subscription = Subscription()
        subscription.add(subscriber)

        val dir = opendir(path)

        if (dir == null) {
            subscriber.error(Error("Unable to obtain directory for path $path"))
            subscription.unsubscribe()
        } else {
            val dirIter = DirectoryIterator(dir)
            for (dirent in dirIter) {
                subscriber.next(dirent.pointed.d_name.toKString())
            }
            subscriber.complete()
        }
        closedir(dir)

        subscription
    }
}