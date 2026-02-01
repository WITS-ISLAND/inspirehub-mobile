import SwiftUI
import Shared
import Combine

class DetailViewModelWrapper: ObservableObject {
    private let viewModel: DetailViewModel
    private var cancellables = Set<AnyCancellable>()

    @Published var selectedNode: Node? = nil
    @Published var comments: [Comment] = []
    @Published var childNodes: [Node] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var commentText: String = ""
    @Published var isCommentSubmitting: Bool = false

    init() {
        self.viewModel = KoinHelper().getDetailViewModel()
        observeViewModel()
    }

    private func observeViewModel() {
        Timer.publish(every: 0.1, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                guard let self = self else { return }

                let newNode = self.viewModel.selectedNode.value as? Node
                if self.selectedNode?.id != newNode?.id { self.selectedNode = newNode }

                let newComments = self.viewModel.comments.value as? [Comment] ?? []
                if self.comments.count != newComments.count || self.comments.map(\.id) != newComments.map(\.id) {
                    self.comments = newComments
                }

                let newChildNodes = self.viewModel.childNodes.value as? [Node] ?? []
                if self.childNodes.count != newChildNodes.count || self.childNodes.map(\.id) != newChildNodes.map(\.id) {
                    self.childNodes = newChildNodes
                }

                let newIsLoading = self.viewModel.isLoading.value as! Bool
                if self.isLoading != newIsLoading { self.isLoading = newIsLoading }

                let newError = self.viewModel.error.value as? String
                if self.error != newError { self.error = newError }

                let newCommentText = self.viewModel.commentText.value as? String ?? ""
                if self.commentText != newCommentText { self.commentText = newCommentText }

                let newIsCommentSubmitting = self.viewModel.isCommentSubmitting.value as! Bool
                if self.isCommentSubmitting != newIsCommentSubmitting { self.isCommentSubmitting = newIsCommentSubmitting }
            }
            .store(in: &cancellables)
    }

    func loadDetail(nodeId: String) {
        viewModel.loadDetail(nodeId: nodeId)
    }

    func toggleLike() {
        viewModel.toggleLike()
    }

    func updateCommentText(_ text: String) {
        viewModel.updateCommentText(text: text)
    }

    func submitComment() {
        viewModel.submitComment()
    }

    func selectNode(node: Node) {
        viewModel.selectNode(node: node)
    }
}
