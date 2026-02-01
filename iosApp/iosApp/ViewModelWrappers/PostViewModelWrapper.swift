import SwiftUI
import Shared
import Combine

class PostViewModelWrapper: ObservableObject {
    private let viewModel: PostViewModel
    private var cancellables = Set<AnyCancellable>()

    @Published var title: String = ""
    @Published var content: String = ""
    @Published var tags: [String] = []
    @Published var parentNode: Node? = nil
    @Published var isSubmitting: Bool = false
    @Published var error: String? = nil
    @Published var isSuccess: Bool = false

    init() {
        self.viewModel = KoinHelper().getPostViewModel()
        observeViewModel()
    }

    private func observeViewModel() {
        Timer.publish(every: 0.1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self else { return }

                let newTitle = self.viewModel.title.value as! String
                if self.title != newTitle { self.title = newTitle }

                let newContent = self.viewModel.content.value as! String
                if self.content != newContent { self.content = newContent }

                let newTags: [String]
                if let arr = self.viewModel.tags.value as? NSArray {
                    newTags = arr.compactMap { $0 as? String }
                } else {
                    newTags = self.viewModel.tags.value as? [String] ?? []
                }
                if self.tags != newTags { self.tags = newTags }

                let newParentNode = self.viewModel.parentNode.value as? Node
                if self.parentNode?.id != newParentNode?.id { self.parentNode = newParentNode }

                let newIsSubmitting = self.viewModel.isSubmitting.value as! Bool
                if self.isSubmitting != newIsSubmitting { self.isSubmitting = newIsSubmitting }

                let newError = self.viewModel.error.value as? String
                if self.error != newError { self.error = newError }

                let newIsSuccess = self.viewModel.isSuccess.value as! Bool
                if self.isSuccess != newIsSuccess { self.isSuccess = newIsSuccess }
            }
            .store(in: &cancellables)
    }

    func updateTitle(_ value: String) {
        viewModel.updateTitle(value: value)
    }

    func updateContent(_ value: String) {
        viewModel.updateContent(value: value)
    }

    func addTag(_ tag: String) {
        viewModel.addTag(tag: tag)
    }

    func removeTag(_ tag: String) {
        viewModel.removeTag(tag: tag)
    }

    func setParentNode(_ node: Node?) {
        viewModel.setParentNode(node: node)
    }

    func submitIssue() {
        viewModel.submitIssue()
    }

    func submitIdea() {
        viewModel.submitIdea()
    }

    func submitDerived() {
        viewModel.submitDerived()
    }

    func reset() {
        viewModel.reset()
    }
}
